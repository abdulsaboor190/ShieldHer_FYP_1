package com.shieldher.vault.files

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.InputStream

class EncryptedFileStore(
    private val context: Context,
    /** Name of the subdirectory inside filesDir. Override in tests to isolate file ops. */
    private val dirName: String = "evidence"
) {

    private val evidenceDir: File
        get() = File(context.filesDir, dirName).also { it.mkdirs() }

    private fun buildEncryptedFile(targetFile: File): EncryptedFile {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedFile.Builder(
            context,
            targetFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    fun write(fileName: String, input: InputStream): File {
        val targetFile = File(evidenceDir, fileName)
        // EncryptedFile.openFileOutput() requires the file to not already exist
        if (targetFile.exists()) targetFile.delete()

        val encryptedFile = buildEncryptedFile(targetFile)
        try {
            encryptedFile.openFileOutput().use { output ->
                input.copyTo(output)
            }
        } catch (e: Throwable) {
            try {
                targetFile.delete()
            } catch (_: Throwable) {
                // best-effort cleanup
            }
            throw e
        }
        return targetFile
    }

    fun read(fileName: String): InputStream {
        val targetFile = File(evidenceDir, fileName)
        val encryptedFile = buildEncryptedFile(targetFile)
        return encryptedFile.openFileInput()
    }

    fun delete(fileName: String): Boolean {
        return File(evidenceDir, fileName).delete()
    }

    fun pathFor(fileName: String): String {
        return File(evidenceDir, fileName).absolutePath
    }

    fun listFileNames(): List<String> {
        return evidenceDir.listFiles()?.map { it.name } ?: emptyList()
    }
}
