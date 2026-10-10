package com.shieldher.vault.data

import android.content.Context
import android.util.Log
import com.shieldher.vault.files.EncryptedFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import androidx.annotation.VisibleForTesting

const val MAX_EVIDENCE_FILE_SIZE_BYTES = 500L * 1024 * 1024 // 500 MB

class EvidenceTooLargeException(val sizeBytes: Long) :
    Exception("File exceeds maximum allowed size of $MAX_EVIDENCE_FILE_SIZE_BYTES bytes (read: $sizeBytes bytes)")

class EvidenceRepository(
    private val context: Context,
    /** Override in tests only — use a directory name that is NOT "evidence". */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val evidenceDirName: String = "evidence",
    /** Override in tests only — size limit for intake streaming. */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val maxFileSizeBytes: Long = MAX_EVIDENCE_FILE_SIZE_BYTES
) {

    companion object {
        private const val TAG = "ShieldHer"
        const val MAX_EVIDENCE_FILE_SIZE_BYTES = com.shieldher.vault.data.MAX_EVIDENCE_FILE_SIZE_BYTES
        private val inFlightIds = ConcurrentHashMap.newKeySet<String>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun addInFlightIdForTesting(id: String) {
            inFlightIds.add(id)
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun removeInFlightIdForTesting(id: String) {
            inFlightIds.remove(id)
        }
    }

    private val db = EvidenceDatabase.getInstance(context)
    private val dao = db.evidenceDao()
    private val fileStore = EncryptedFileStore(context, evidenceDirName)

    fun getAllEvidence(): Flow<List<EvidenceEntity>> {
        return dao.getAll()
    }

    suspend fun sweepOrphans(): Int = withContext(Dispatchers.IO) {
        val filesOnDisk = fileStore.listFileNames()
        var deletedCount = 0
        for (fileName in filesOnDisk) {
            // (a) skip if the id is in inFlightIds
            if (inFlightIds.contains(fileName)) {
                continue
            }
            // (b) then skip if dao.getById(id) is not null
            if (dao.getById(fileName) != null) {
                continue
            }
            // (c) otherwise delete
            if (fileStore.delete(fileName)) {
                deletedCount++
            }
        }
        Log.i(TAG, "sweepOrphans deleted $deletedCount orphaned files")
        deletedCount
    }

    suspend fun intake(
        input: InputStream,
        mimeType: String,
        sourceLabel: String?
    ): EvidenceEntity {
        val id = UUID.randomUUID().toString()
        inFlightIds.add(id)
        try {
            // Compute SHA-256 and enforce size limit while writing in a single streaming pass
            val digest = MessageDigest.getInstance("SHA-256")
            val limitingStream = LimitingInputStream(input, maxFileSizeBytes)
            val digestStream = DigestInputStream(limitingStream, digest)

            val writtenFile = try {
                fileStore.write(id, digestStream)
            } catch (e: IOException) {
                Log.e(TAG, "intake failed: ${e.javaClass.simpleName}")
                try {
                    fileStore.delete(id)
                } catch (cleanupEx: Throwable) {
                    Log.w(TAG, "cleanup failed: ${cleanupEx.javaClass.simpleName}")
                }
                throw e
            } catch (e: Throwable) {
                try {
                    fileStore.delete(id)
                } catch (cleanupEx: Throwable) {
                    Log.w(TAG, "cleanup failed: ${cleanupEx.javaClass.simpleName}")
                }
                throw e
            }

            val hash = digest.digest().toHex()
            val receiptTimeEpochMillis = System.currentTimeMillis()
            val timeZoneId = TimeZone.getDefault().id
            val fileSizeBytes = writtenFile.length()
            val filePath = fileStore.pathFor(id)

            val entity = EvidenceEntity(
                id = id,
                hash = hash,
                receiptTimeEpochMillis = receiptTimeEpochMillis,
                timeZoneId = timeZoneId,
                sourceLabel = sourceLabel,
                mimeType = mimeType,
                fileSizeBytes = fileSizeBytes,
                filePath = filePath,
                parentId = null,
                transformationType = null,
                deleted = false
            )

            dao.insert(entity)
            return entity
        } finally {
            inFlightIds.remove(id)
        }
    }

    suspend fun verifyIntegrity(id: String): Boolean {
        val entity = dao.getById(id) ?: return false

        return try {
            val currentDigest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            fileStore.read(entity.id).use { input ->
                DigestInputStream(input, currentDigest).use { dis ->
                    while (dis.read(buffer) != -1) {
                        // drain stream to feed the digest; bytes discarded
                    }
                }
            }
            val currentHash = currentDigest.digest().toHex()
            currentHash.equals(entity.hash, ignoreCase = true)
        } catch (e: Exception) {
            Log.w(TAG, "verify failed: ${e.javaClass.simpleName}")
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        val entity = dao.getById(id) ?: return false

        // Recursively delete all derived artifacts first
        val derived = dao.getDerived(id)
        for (artifact in derived) {
            delete(artifact.id)
        }

        // Delete the encrypted file from disk
        val fileDeleted = fileStore.delete(entity.id)
        if (!fileDeleted) {
            Log.w(TAG, "delete: file not found on disk")
        }

        // Hard-delete the DB row regardless of whether the file existed
        dao.hardDelete(entity.id)

        // Clear any temporary cache files associated with this id
        clearCacheFor(entity.id)
        return true
    }

    fun clearCacheFor(id: String) {
        val cacheFiles = context.cacheDir?.listFiles() ?: return
        for (file in cacheFiles) {
            if (file.name.contains(id)) {
                file.delete()
            }
        }
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { byte -> "%02x".format(byte) }

    private class LimitingInputStream(
        private val delegate: InputStream,
        private val maxBytes: Long
    ) : InputStream() {
        private var totalBytesRead = 0L

        override fun read(): Int {
            val b = delegate.read()
            if (b != -1) {
                checkLimit(1)
            }
            return b
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val count = delegate.read(b, off, len)
            if (count > 0) {
                checkLimit(count.toLong())
            }
            return count
        }

        private fun checkLimit(additional: Long) {
            totalBytesRead += additional
            if (totalBytesRead > maxBytes) {
                throw EvidenceTooLargeException(totalBytesRead)
            }
        }

        override fun close() {
            delegate.close()
        }
    }
}
