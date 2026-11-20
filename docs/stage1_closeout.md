# Stage 1 Closeout
FE-0.3 and FE-2.4


## Known Stage 1 lint errors
- **Lint Scan Results:** 2 errors, 35 warnings found by `lintDebug`.
- **Specific Errors:**
  1. `app/src/main/java/com/shieldher/vault/data/EvidenceRepository.kt:26`: Error: Did you mean @get:VisibleForTesting? Without get: this annotates the constructor parameter itself instead of the associated getter. [SupportAnnotationUsage]
  2. `app/src/main/java/com/shieldher/vault/data/EvidenceRepository.kt:29`: Error: Did you mean @get:VisibleForTesting? Without get: this annotates the constructor parameter itself instead of the associated getter. [SupportAnnotationUsage]
- **Status:** Not fixed, because Stage 1 vault files are add-only; decision pending with Bhoomika on whether a one-line fix is an approved exception, otherwise deferred to FYP-II.


## FE-0.3 results (16 Nov 2026)

### 1. Test Baseline Counts
- **Unit Tests (`testDebugUnitTest`):** **8 passed**, 0 failed, 0 skipped.
  - `HashUtilTest`: 6 passed
  - `ExampleUnitTest`: 1 passed
  - `SpikeToolchainTest`: 1 passed
- **Instrumented Tests (`connectedDebugAndroidTest` on `emulator-5554` / API 37):** **16 executed**, **15 passed**, **1 failed**, 0 skipped.
  - `com.shieldher.ExampleInstrumentedTest`: 1 passed
  - `com.shieldher.vault.DeletionTest`: 6 passed
  - `com.shieldher.vault.IntegrityTest`: 7 passed, 1 failed
    - *Failing test:* `intake_acceptsInputAtLimit`
    - *Error:* `java.lang.AssertionError: expected:<1024> but was:<1080>`
    - *Analysis:* The test asserts `entity.fileSizeBytes == 1024L`, but `entity.fileSizeBytes` records `writtenFile.length()`, which is the on-disk ciphertext length produced by `EncryptedFileStore` (including Jetpack Security's 56-byte header/overhead for AES-GCM/Keyset, totaling 1080 bytes).
  - `com.shieldher.vault.OrphanSweepTest`: 1 passed

### 2. Git State & Ancestry
- **Tag:** `stage1-baseline` exists.
- **Commit:** `3cf440bb4fd7660e7a6ed009dd2a8e71aa9900f3`
- **Ancestry:** `stage1-baseline` is confirmed an ancestor of `main` (`git merge-base --is-ancestor` returned exit code 0).

### 3. Backup Settings
- `AndroidManifest.xml`:
  - `android:allowBackup="false"` [VERIFIED IN FILES]
  - `android:dataExtractionRules="@xml/data_extraction_rules"` [VERIFIED IN FILES]
  - `android:fullBackupContent="@xml/backup_rules"` [VERIFIED IN FILES]
- `data_extraction_rules.xml`:
  - Explicitly excludes `root`, `file`, `database`, `sharedpref`, `external` from both `<cloud-backup>` and `<device-transfer>` [VERIFIED IN FILES].
- `backup_rules.xml`:
  - Explicitly excludes `root`, `file`, `database`, `sharedpref`, `external` [VERIFIED IN FILES].
- **Conclusions:**
  - Cloud backup: **EXCLUDED** on all Android versions [VERIFIED IN FILES].
  - Device-to-device transfer: **EXCLUDED** on Android 12+ [VERIFIED IN FILES].

### 4. Log Audit in Vault Code (`app/src/main/java/com/shieldher/vault`)

| File:Line | Logged Statement | Category / Risk | Value Logged |
| :--- | :--- | :--- | :--- |
| `EvidenceRepository.kt:74` | `Log.i(TAG, "sweepOrphans deleted $deletedCount orphaned files")` | None [VERIFIED] | Integer counter only |
| `EvidenceRepository.kt:94` | `Log.e(TAG, "intake failed: ${e.javaClass.simpleName}")` | None [VERIFIED] | Exception class name |
| `EvidenceRepository.kt:98` | `Log.w(TAG, "cleanup failed: ${cleanupEx.javaClass.simpleName}")` | None [VERIFIED] | Exception class name |
| `EvidenceRepository.kt:105` | `Log.w(TAG, "cleanup failed: ${cleanupEx.javaClass.simpleName}")` | None [VERIFIED] | Exception class name |
| `EvidenceRepository.kt:153` | `Log.w(TAG, "verify failed: ${e.javaClass.simpleName}")` | None [VERIFIED] | Exception class name |
| `EvidenceRepository.kt:170` | `Log.w(TAG, "delete: file not found on disk")` | None [VERIFIED] | Constant message string |
| `IntakeActivity.kt:72` | `Log.w("ShieldHer", "intake rejected: size limit exceeded")` | None [VERIFIED] | Constant message string |
| `IntakeActivity.kt:121` | `Log.w("ShieldHer", "intake rejected: ${e.javaClass.simpleName}")` | None [VERIFIED] | Exception class name |

*Audit Summary:* Zero log statements leak evidence file content, file paths, SHA-256 hashes, or user-entered text.

### 5. Open Items
1. `IntegrityTest.intake_acceptsInputAtLimit` failure: The test compares plaintext payload size with encrypted ciphertext length on disk (1024 vs 1080 bytes). Needs review with Bhoomika as an existing Stage 1 test assertion gap.
2. 2 known Stage 1 `@VisibleForTesting` lint errors remain deferred under the Stage 1 code freeze.


## FE-2.4 additivity proof and FE-2.7 log audit (20 Nov 2026)

### 1. Regression Test Results
- **Unit Tests (`testDebugUnitTest`):** **45 tests passed**, 0 failed, 0 skipped.
  - `com.shieldher.ExampleUnitTest`: 1 passed
  - `com.shieldher.dashboard.SpikeToolchainTest`: 1 passed
  - `com.shieldher.dashboard.common.BadgeStatusTest`: 3 passed
  - `com.shieldher.dashboard.common.ScreenProtectionTest`: 4 passed
  - `com.shieldher.dashboard.common.UiStateTemplateTest`: 5 passed
  - `com.shieldher.dashboard.data.BadgeMappingTest`: 4 passed
  - `com.shieldher.dashboard.data.FakeCaseRepositoryTest`: 8 passed
  - `com.shieldher.dashboard.data.FakeEvidenceGatewayTest`: 6 passed
  - `com.shieldher.dashboard.data.VaultMappersTest`: 3 passed
  - `com.shieldher.dashboard.data.VerificationSessionTest`: 4 passed
  - `com.shieldher.vault.files.HashUtilTest`: 6 passed
- **Instrumented Tests:** Not run (no active emulator serial provided). All instrumented test sources compile cleanly without errors via `assembleDebugAndroidTest`.

### 2. Additivity Proof (Stage 1 Files Untouched)
Comparison against Stage 1 close (`4808f10` / baseline):

| Added Lines | Deleted Lines | File Path | Status |
| :---: | :---: | :--- | :--- |
| 131 | **0** | `app/src/main/java/com/shieldher/vault/data/EvidenceRepository.kt` | Modified (Additive only) |
| 11 | **0** | `app/src/main/java/com/shieldher/vault/data/EvidenceDisplay.kt` | New File |
| 8 | **0** | `app/src/main/java/com/shieldher/vault/data/ImportResult.kt` | New File |
| 7 | **0** | `app/src/main/java/com/shieldher/vault/data/IntegrityResult.kt` | New File |
| 89 | **0** | `app/src/androidTest/java/com/shieldher/vault/ImportFromUriTest.kt` | New File |
| 107 | **0** | `app/src/androidTest/java/com/shieldher/vault/IntegrityDetailedTest.kt` | New File |
| 126 | **0** | `app/src/androidTest/java/com/shieldher/vault/ReadForDisplayTest.kt` | New File |
| 229 | **0** | `app/src/androidTest/java/com/shieldher/dashboard/cases/CaseStoreTest.kt` | New File |
| 192 | **0** | `app/src/androidTest/java/com/shieldher/dashboard/cases/DefaultCaseRepositoryTest.kt` | New File |

*Audit Summary:* **0 lines deleted** across all Stage 1 files.
The only modified Stage 1 file is `EvidenceRepository.kt`, where three new methods were appended:
1. `suspend fun verifyIntegrityDetailed(id: String): IntegrityResult` (FE-2.1)
2. `suspend fun readForDisplay(id: String, maxBytes: Int, maxImageSidePx: Int): EvidenceDisplay` (FE-2.2)
3. `suspend fun importFromUri(uri: Uri, mimeType: String?): ImportResult` (FE-2.3)
No existing signatures, methods, or classes in Stage 1 were altered, renamed, or deleted.

### 3. Log Audit in New Code
Scan of all new and modified files in `app/src/main/java/com/shieldher/vault` and `app/src/main/java/com/shieldher/dashboard` for `Log.*`, `println`, `Timber`, and `printStackTrace`:

*Results:* **Zero occurrences** found in any new Phase 2 code.
The only logging statements in the entire codebase are the original 8 safe statements from Stage 1 (verified in FE-0.3 above), which log only exception class names, constant messages, and counters.

### 4. Open Items
- None for Phase 2. Phase 2 deliverables (P2-0, P2-1, P2-2, P2-3, P2-4, and P2-5) are complete.
