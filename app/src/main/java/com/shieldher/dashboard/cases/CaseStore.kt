package com.shieldher.dashboard.cases

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.shieldher.dashboard.data.Case
import com.shieldher.dashboard.data.CaseItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

/**
 * Encrypted persistent store for cases and their items.
 *
 * Content is a single JSON document encrypted with a Keystore-backed MasterKey
 * (alias [KEY_ALIAS]). The document is never written in place: writes go to a
 * ".tmp" file first, then the current file is renamed to ".bak", then ".tmp" is
 * renamed to the real name, and ".bak" is deleted. A crash at any point leaves at
 * least one readable copy.
 *
 * If [load] finds the real file missing but a ".bak" present it restores the backup.
 * If a stale ".tmp" is present it is trusted only if it decrypts and parses correctly;
 * otherwise it is deleted.
 *
 * If the real file exists but cannot be decrypted or parsed, the store enters UNREADABLE
 * state. In that state reads return the last in-memory snapshot (empty on first load),
 * the damaged file is left untouched, and every write attempt throws [CaseStoreException].
 *
 * No logging of content, paths, ids, titles or notes anywhere in this class.
 */
class CaseStore(
    private val context: Context,
    private val directory: File,
    private val fileName: String = DEFAULT_FILE_NAME
) {
    companion object {
        const val DEFAULT_FILE_NAME = "cases.enc"
        private const val KEY_ALIAS = "shieldher_cases_master_key"
    }

    private val mutex = Mutex()

    private val _cases = MutableStateFlow<List<Case>>(emptyList())
    val cases: StateFlow<List<Case>> = _cases.asStateFlow()

    private val _items = MutableStateFlow<List<CaseItem>>(emptyList())
    val items: StateFlow<List<CaseItem>> = _items.asStateFlow()

    @Volatile private var unreadable = false

    // ── File helpers ──────────────────────────────────────────────────────────

    private val realFile: File get() = File(directory, fileName)
    private val tmpFile: File get() = File(directory, "$fileName.tmp")
    private val bakFile: File get() = File(directory, "$fileName.bak")

    private fun buildMasterKey(): MasterKey =
        MasterKey.Builder(context, KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private fun buildEncryptedFile(target: File): EncryptedFile =
        EncryptedFile.Builder(context, target, buildMasterKey(), EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB)
            .build()

    // ── Read helpers ──────────────────────────────────────────────────────────

    private fun tryDecryptAndParse(file: File): CaseJson.StoreDoc? {
        if (!file.exists()) return null
        return try {
            val bytes = buildEncryptedFile(file).openFileInput().use { it.readBytes() }
            CaseJson.decode(String(bytes, Charsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Loads the store from disk. Must be called once before any other operation.
     * Safe to call multiple times (idempotent after the first successful load).
     */
    suspend fun load() = mutex.withLock { loadLocked() }

    private fun loadLocked() {
        // Recovery: if real file missing but .bak exists, restore backup.
        if (!realFile.exists() && bakFile.exists()) {
            bakFile.renameTo(realFile)
        }

        // If real file still missing, store is simply empty — not an error.
        if (!realFile.exists()) {
            // Handle stale .tmp: trust it only if it parses correctly.
            val tmpDoc = tryDecryptAndParse(tmpFile)
            if (tmpDoc != null) {
                // Stale tmp from a previous partial write — restore it.
                tmpFile.renameTo(realFile)
                applyDoc(tmpDoc)
            } else {
                tmpFile.delete()
            }
            return
        }

        // Real file exists — try to read it.
        val doc = tryDecryptAndParse(realFile)
        if (doc != null) {
            // Clean up any leftover tmp.
            tmpFile.delete()
            applyDoc(doc)
        } else {
            // Real file is damaged — enter UNREADABLE state; leave file alone.
            unreadable = true
        }
    }

    private fun applyDoc(doc: CaseJson.StoreDoc) {
        _cases.value = doc.cases
        _items.value = doc.items
    }

    // ── Write helpers ─────────────────────────────────────────────────────────

    private fun requireWritable() {
        if (unreadable) throw CaseStoreException(CaseStoreException.Reason.UNREADABLE)
    }

    /**
     * Override point for testing: called between writing tmp and replacing real file.
     * Tests can inject a failure here to verify crash-safety. Default is a no-op.
     */
    internal var afterTmpWrittenHook: (() -> Unit)? = null

    private fun persistLocked(cases: List<Case>, items: List<CaseItem>) {
        requireWritable()
        directory.mkdirs()

        // 1. Write to tmp (EncryptedFile refuses to overwrite; delete first).
        tmpFile.delete()
        buildEncryptedFile(tmpFile).openFileOutput().use { out ->
            out.write(CaseJson.encode(cases, items).toByteArray(Charsets.UTF_8))
        }

        // Test hook: simulate crash after tmp written.
        afterTmpWrittenHook?.invoke()

        // 2. Rename real → bak.
        if (realFile.exists()) {
            bakFile.delete()
            realFile.renameTo(bakFile)
        }

        // 3. Rename tmp → real.
        tmpFile.renameTo(realFile)

        // 4. Delete bak.
        bakFile.delete()

        _cases.value = cases
        _items.value = items
    }

    // ── Mutation API ──────────────────────────────────────────────────────────

    suspend fun saveCases(cases: List<Case>, items: List<CaseItem>) = mutex.withLock {
        persistLocked(cases, items)
    }

    suspend fun deleteEverything() = mutex.withLock {
        requireWritable()
        realFile.delete()
        tmpFile.delete()
        bakFile.delete()
        _cases.value = emptyList()
        _items.value = emptyList()
        unreadable = false
    }

    // ── State query ───────────────────────────────────────────────────────────

    val isUnreadable: Boolean get() = unreadable
}
