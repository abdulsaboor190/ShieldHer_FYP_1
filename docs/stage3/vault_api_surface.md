# Stage 1 Vault API Surface

**Date:** 18 November 2026  
**Document version:** 1.0  
**Purpose:** Technical audit of Stage 1 Evidence Vault API surface and internals to guide Phase 2 additive extensions without modifying existing Stage 1 behavior or breaking isolation.

---

## 1. EvidenceRepository
* **File path:** `app/src/main/java/com/shieldher/vault/data/EvidenceRepository.kt` [VERIFIED IN CODE]
* **Constructor parameters:**
  * `context: Context` [VERIFIED IN CODE]
  * `@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) internal val evidenceDirName: String = "evidence"` [VERIFIED IN CODE]
  * `@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) internal val maxFileSizeBytes: Long = MAX_EVIDENCE_FILE_SIZE_BYTES` (default: 500 MB) [VERIFIED IN CODE]
* **Companion / Constants:**
  * `MAX_EVIDENCE_FILE_SIZE_BYTES: Long = 500L * 1024 * 1024` (500 MB) [VERIFIED IN CODE]
  * `inFlightIds = ConcurrentHashMap.newKeySet<String>()` (shared set across all repository instances) [VERIFIED IN CODE]
  * `addInFlightIdForTesting(id: String)`: Adds an id to in-flight set for test simulation [VERIFIED IN CODE]
  * `removeInFlightIdForTesting(id: String)`: Removes an id from in-flight set for test simulation [VERIFIED IN CODE]
* **Public / Internal Members:**
  * `fun getAllEvidence(): Flow<List<EvidenceEntity>>`: Returns a cold `Flow` of non-deleted evidence ordered by receipt timestamp descending directly from Room DAO [VERIFIED IN CODE].
  * `suspend fun sweepOrphans(): Int`: Runs on `Dispatchers.IO`; scans file store directory and deletes any file whose name is NOT in `inFlightIds` and NOT found in `EvidenceDao.getById(id)`. Returns the count of deleted files [VERIFIED IN CODE].
  * `suspend fun intake(input: InputStream, mimeType: String, sourceLabel: String?): EvidenceEntity`: Streams an input stream through SHA-256 calculation and size limit enforcement (500 MB) directly into encrypted storage, writes an `EvidenceEntity` to Room, and returns the persisted entity [VERIFIED IN CODE].
  * `suspend fun verifyIntegrity(id: String): Boolean`: Re-hashes the stored encrypted file on disk by streaming it through `EncryptedFileStore.read(id)`, and returns `true` if its SHA-256 hex equals `entity.hash` (case-insensitive); returns `false` if row not found, file missing, corrupted, or exception thrown [VERIFIED IN CODE].
  * `suspend fun delete(id: String): Boolean`: Cascades recursively to delete derived artifacts, removes the encrypted file from disk via `fileStore.delete(id)`, hard-deletes the database row via `dao.hardDelete(id)`, and clears temporary cache files matching the id via `clearCacheFor(id)`. Returns `true` if row existed, `false` otherwise [VERIFIED IN CODE].
  * `fun clearCacheFor(id: String)`: Cleans any file in `context.cacheDir` whose name contains `id` [VERIFIED IN CODE].

---

## 2. Room Entity and DAO
* **Entity Class:** `com.shieldher.vault.data.EvidenceEntity` [VERIFIED IN CODE]
* **Table Name:** `evidence` [VERIFIED IN CODE]
* **Fields:**
  * `id: String` (Primary Key, default: `UUID.randomUUID().toString()`, non-null) [VERIFIED IN CODE]
  * `hash: String` (SHA-256 hex string, non-null) [VERIFIED IN CODE]
  * `receiptTimeEpochMillis: Long` (Epoch milliseconds timestamp, non-null) [VERIFIED IN CODE]
  * `timeZoneId: String` (Time zone string e.g. "Asia/Karachi", non-null) [VERIFIED IN CODE]
  * `sourceLabel: String?` (Optional string, nullable) [VERIFIED IN CODE]
  * `mimeType: String` (MIME type string e.g. "image/jpeg", "text/plain", non-null) [VERIFIED IN CODE]
  * `fileSizeBytes: Long` (Length of encrypted file written to storage, non-null) [VERIFIED IN CODE]
  * `filePath: String` (Absolute storage path, non-null) [VERIFIED IN CODE]
  * `parentId: String?` (Optional parent evidence id for derived artifacts, nullable) [VERIFIED IN CODE]
  * `transformationType: String?` (Optional transformation tag, nullable) [VERIFIED IN CODE]
  * `deleted: Boolean` (Soft delete flag, default `false`, non-null) [VERIFIED IN CODE]
* **DAO Interface:** `com.shieldher.vault.data.EvidenceDao` [VERIFIED IN CODE]
* **DAO Methods:**
  * `suspend fun insert(evidence: EvidenceEntity)` [VERIFIED IN CODE]
  * `fun getAll(): Flow<List<EvidenceEntity>>` (Query: `SELECT * FROM evidence WHERE deleted = 0 ORDER BY receiptTimeEpochMillis DESC`) [VERIFIED IN CODE]
  * `suspend fun getById(id: String): EvidenceEntity?` (Query: `SELECT * FROM evidence WHERE id = :id`) [VERIFIED IN CODE]
  * `suspend fun getDerived(parentId: String): List<EvidenceEntity>` (Query: `SELECT * FROM evidence WHERE parentId = :parentId`) [VERIFIED IN CODE]
  * `suspend fun markDeleted(id: String)` (Query: `UPDATE evidence SET deleted = 1 WHERE id = :id`) [VERIFIED IN CODE]
  * `suspend fun hardDelete(id: String)` (Query: `DELETE FROM evidence WHERE id = :id`) [VERIFIED IN CODE]

---

## 3. File Store
* **Class Name:** `com.shieldher.vault.files.EncryptedFileStore` [VERIFIED IN CODE]
* **Constructor:**
  * `context: Context` [VERIFIED IN CODE]
  * `dirName: String = "evidence"` (subfolder inside `context.filesDir`) [VERIFIED IN CODE]
* **Storage Location & Naming:**
  * Root directory: `File(context.filesDir, dirName)` (created on access via `mkdirs()`) [VERIFIED IN CODE].
  * Stored file name: Derived directly from `entity.id` (UUID string), e.g., `filesDir/evidence/<id>` [VERIFIED IN CODE].
* **Encryption Scheme & Keys:**
  * Key management: `MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()` [VERIFIED IN CODE].
  * Encryption scheme: `EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB` [VERIFIED IN CODE].
  * Encrypted file construction: built on-the-fly per operation wrapping target file [VERIFIED IN CODE].
* **API Methods:**
  * `fun write(fileName: String, input: InputStream): File`: Deletes pre-existing file if present, opens output stream via `encryptedFile.openFileOutput()`, copies input, and cleans up on failure [VERIFIED IN CODE].
  * `fun read(fileName: String): InputStream`: Returns decrypted `InputStream` via `encryptedFile.openFileInput()` [VERIFIED IN CODE].
  * `fun delete(fileName: String): Boolean`: Deletes file from disk [VERIFIED IN CODE].
  * `fun pathFor(fileName: String): String`: Returns absolute file path string [VERIFIED IN CODE].
  * `fun listFileNames(): List<String>`: Returns file names present in storage directory [VERIFIED IN CODE].

---

## 4. intake()
* **Signature:**
  ```kotlin
  suspend fun intake(
      input: InputStream,
      mimeType: String,
      sourceLabel: String?
  ): EvidenceEntity
  ```
* **Parameters:**
  * `input`: `InputStream` containing incoming raw evidence bytes.
  * `mimeType`: String indicating MIME type.
  * `sourceLabel`: Optional String for caller/source attribution (e.g., referrer host or "In-app import").
* **Size Limit & Enforcement:**
  * Maximum size: 500 MB (`MAX_EVIDENCE_FILE_SIZE_BYTES = 500L * 1024 * 1024`).
  * Enforced on-the-fly using internal `LimitingInputStream` during writing. Throws `EvidenceTooLargeException(totalBytesRead)` as soon as byte limit is exceeded [VERIFIED IN CODE].
* **Exceptions & Errors:**
  * `EvidenceTooLargeException`: when incoming data exceeds 500 MB.
  * `IOException` / `Throwable`: from stream reading, cryptographic writing, or DAO insertion.
  * Cleanup guarantee: catch block attempts `fileStore.delete(id)` if writing fails [VERIFIED IN CODE].
* **In-Flight Set Handling:**
  * `inFlightIds.add(id)` called before file writing starts; removed in `finally` block [VERIFIED IN CODE].
* **Hashing & Storage:**
  * SHA-256 digest is calculated simultaneously via `DigestInputStream` during stream copy to `fileStore.write(id, digestStream)` [VERIFIED IN CODE].
  * Persisted values: `id` (UUID), `hash` (hex), `receiptTimeEpochMillis` (`System.currentTimeMillis()`), `timeZoneId` (`TimeZone.getDefault().id`), `sourceLabel`, `mimeType`, `fileSizeBytes`, `filePath`, `parentId = null`, `transformationType = null`, `deleted = false`.

---

## 5. verifyIntegrity()
* **Behavior:**
  1. Retrieves row by id: `dao.getById(id)`. If `null`, returns `false` [VERIFIED IN CODE].
  2. Wraps `fileStore.read(entity.id)` in `DigestInputStream` with SHA-256 digest and drains entire decrypted stream into 8KB buffer [VERIFIED IN CODE].
  3. Compares calculated hash hex against stored `entity.hash` with `equalsIgnoreCase`. Returns `true` on match [VERIFIED IN CODE].
  4. If file is missing, truncated, corrupted, or cannot be decrypted, `fileStore.read(...)` or stream drain throws an exception; caught in `try-catch` block returning `false` [VERIFIED IN CODE].
* **HashUtil API:**
  * `HashUtil.sha256(bytes: ByteArray): String` [VERIFIED IN CODE]
  * `HashUtil.sha256Stream(input: InputStream): String` [VERIFIED IN CODE]

---

## 6. Delete
* **Delete-by-id:**
  * **Repository:** `suspend fun delete(id: String): Boolean` exists on `EvidenceRepository` [VERIFIED IN CODE].
  * **DAO:** `dao.hardDelete(id)` and `dao.getDerived(id)` exist [VERIFIED IN CODE].
  * **FileStore:** `fileStore.delete(fileName)` exists [VERIFIED IN CODE].
* **Order of Execution in `delete(id)`:**
  1. Looks up entity by `id`; returns `false` if not found.
  2. Recursively calls `delete(derived.id)` for any derived artifacts (`dao.getDerived(id)`).
  3. Deletes encrypted file on disk (`fileStore.delete(entity.id)`).
  4. Hard-deletes database row (`dao.hardDelete(entity.id)`).
  5. Clears any cached files in `cacheDir` matching the `id` (`clearCacheFor(entity.id)`).
* **`deleteAll`:** Does **NOT** exist in `EvidenceRepository` or `EvidenceDao` [VERIFIED IN CODE]. However, deleting all items can be achieved cleanly by iterating over `dao.getAll()` and invoking `delete(entity.id)` on each [INFERENCE].

---

## 7. Decrypt to Memory
* **Existing API:**
  * `EncryptedFileStore.read(fileName: String): InputStream` already returns a decrypted `InputStream` connected to `encryptedFile.openFileInput()` [VERIFIED IN CODE].
* **Evaluation:**
  * Because `EncryptedFileStore.read()` already exists and is public, `EvidenceRepository` can directly open the decrypted stream without adding any new method to `EncryptedFileStore` [VERIFIED IN CODE].
  * For `readForDisplay(id: String, maxBytes: Int, maxImageSidePx: Int)`: can be implemented purely inside `EvidenceRepository` by calling `fileStore.read(id)`, reading bounds / bytes into memory, and returning `EvidenceDisplay`. No changes or new methods are strictly required on `EncryptedFileStore` [INFERENCE].

---

## 8. Threading and Lifecycle
* **Database Instance:**
  * Singleton managed via `EvidenceDatabase.getInstance(context)` with double-checked locking and in-memory test overrides (`setInstanceForTesting` / `clearInstanceForTesting`) [VERIFIED IN CODE].
* **Repository Lifecycle:**
  * Constructed on-demand with `Context` (e.g. `EvidenceRepository(context)`); internally holds database singleton and `EncryptedFileStore(context, evidenceDirName)` [VERIFIED IN CODE].
* **Dispatchers:**
  * DAO Room operations use Room's internal coroutine dispatchers.
  * Explicit blocking work (e.g. `sweepOrphans()`) runs under `withContext(Dispatchers.IO)` [VERIFIED IN CODE].
* **UI Exposure:**
  * `getAllEvidence()` exposes a cold `Flow<List<EvidenceEntity>>` directly from Room [VERIFIED IN CODE].
  * `EvidenceListViewModel` exposes `evidenceList: Flow<List<EvidenceEntity>>` [VERIFIED IN CODE].

---

## 9. Test Isolation Pattern
* **Existing Test Classes:**
  * `app/src/androidTest/java/com/shieldher/vault/DeletionTest.kt`
  * `app/src/androidTest/java/com/shieldher/vault/IntegrityTest.kt`
  * `app/src/androidTest/java/com/shieldher/vault/OrphanSweepTest.kt`
* **Hermetic Setup & Isolation Pattern:**
  1. **Database:** Built in-memory via `Room.inMemoryDatabaseBuilder(context, EvidenceDatabase::class.java).allowMainThreadQueries().build()` and registered via `EvidenceDatabase.setInstanceForTesting(db)` [VERIFIED IN CODE].
  2. **Storage Directory:** Uses a distinct test folder name passed to repository constructor, e.g. `evidenceDirName = "evidence_test_integrity"` or `"evidence_test_deletion"` [VERIFIED IN CODE].
  3. **Canonical Path Assertion:** Explicit check before testing starts:
     ```kotlin
     check(testDir.canonicalPath != File(context.filesDir, "evidence").canonicalPath) {
         "Test isolation broken: testDir resolved to the real evidence directory"
     }
     ```
  4. **Teardown Cleanup:** In `@After`, recursively deletes test directory (`testDir.deleteRecursively()`), closes in-memory database (`db.close()`), and clears database singleton (`EvidenceDatabase.clearInstanceForTesting()`) [VERIFIED IN CODE].

---

## 10. Feasibility of Phase 2 Additions as Purely Additive Changes
* **FE-2.1 (`verifyIntegrityDetailed`): YES, purely additive [VERIFIED IN CODE].**
  * Requires adding enum `IntegrityResult { UNCHANGED, CHANGED, MISSING }` in a new file `IntegrityResult.kt`.
  * New method `suspend fun verifyIntegrityDetailed(id: String): IntegrityResult` can be appended directly to `EvidenceRepository.kt`.
  * It can check: if `dao.getById(id)` is null -> `MISSING`. Check if file exists via `File(context.filesDir, evidenceDirName, id).exists()` -> if false, `MISSING`. Then attempt stream decrypt and SHA-256 compare; if hash matches -> `UNCHANGED`; if hash mismatches or stream read/decrypt throws `IOException`/`GeneralSecurityException` -> `CHANGED`.
  * Does not modify any existing line or visibility.
* **FE-2.2 (`readForDisplay`): YES, purely additive [VERIFIED IN CODE].**
  * Requires adding sealed interface `EvidenceDisplay` in a new file `EvidenceDisplay.kt`.
  * New method `suspend fun readForDisplay(id: String, maxBytes: Int, maxImageSidePx: Int): EvidenceDisplay` can be appended to `EvidenceRepository.kt`.
  * Uses existing `dao.getById(id)` and `fileStore.read(id)`. Because `fileStore.read()` already exists and is public, no changes to `EncryptedFileStore` are needed.
  * Image downsampling and text UTF-8 decoding happen strictly in memory.
  * Does not modify any existing line or visibility.
* **FE-2.3 (`importFromUri`): YES, purely additive [VERIFIED IN CODE].**
  * Requires adding sealed interface `ImportResult` in a new file `ImportResult.kt`.
  * New method `suspend fun importFromUri(uri: Uri, mimeType: String?): ImportResult` can be appended to `EvidenceRepository.kt`.
  * Can call existing `sweepOrphans()`, open stream from `context.contentResolver.openInputStream(uri)`, and call existing `intake(...)` with `sourceLabel = "In-app import"`.
  * Does not modify any existing line or visibility.
* **Delete-by-id Status:**
  * Delete-by-id (`EvidenceRepository.delete(id: String)`) **already exists** in Stage 1 and is public. **No fourth addition is required.**

---

## 11. Questions for Bhoomika
1. **`deleteAll` semantics**: Stage 1 does not expose a `deleteAll()` method on `EvidenceRepository` or `EvidenceDao`. In Phase 2 `EvidenceGateway.deleteAll()` will loop over `dao.getAll()` and call `delete(id)` for each item. Does Stage 1 have any preference or hidden dependencies for bulk deletion?
2. **File length vs Encrypted size**: `EvidenceEntity.fileSizeBytes` records `writtenFile.length()`, which is the ciphertext file size on disk (including Jetpack Security headers/IV/tag), rather than the original plaintext byte size. `DisplayContent` and `EvidenceItem` will expose this stored size. Is this consistent with Stage 1 expectations?
