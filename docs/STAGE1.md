# ShieldHer — Stage 1 Evidence Vault

## 1. What Stage 1 Contains

Stage 1 implements the core Evidence Vault for ShieldHer:
* **Intake Pipeline:** `IntakeActivity` registers as an Android share target (`ACTION_SEND`) supporting `image/*` and `text/plain`.
* **Receipt Hashing:** SHA-256 hash calculated streaming in a single pass wrapped in `DigestInputStream` during disk write before any other operation touches the bytes.
* **Encrypted Storage:** `EncryptedFileStore` leverages `androidx.security.crypto.EncryptedFile` with AES-256-GCM encryption and keys managed by the hardware-backed Android Keystore (`MasterKey`).
* **Provenance & Metadata:** `EvidenceEntity` stored via Room DB (`EvidenceDatabase`), persisting `id`, `hash`, `receiptTimeEpochMillis`, `timeZoneId`, `sourceLabel`, `mimeType`, `fileSizeBytes`, `filePath`, `parentId`, `transformationType`, and `deleted`.
* **Integrity Verification:** `verifyIntegrity(id)` recalculates the SHA-256 digest on demand over the decrypted stream and verifies it matches the recorded receipt hash, returning clean booleans and catching tampering, truncation, or missing files.
* **Deletion & Cascading:** `delete(id)` deletes derived and grandchild artifacts recursively, deletes ciphertext files from disk, hard-deletes database rows, and purges temporary cache files via `clearCacheFor(id)`.
* **Orphan Sweeping:** `sweepOrphans()` scans disk files at startup and removes unbacked ciphertext files while preserving in-flight write operations.
* **Privacy Hardening:** Auto Backup is disabled (`android:allowBackup="false"` with full domain exclusions), `INTERNET` permission is omitted, and all logging is sanitized under the unified `"ShieldHer"` tag without leaking IDs, hashes, paths, or exception messages.

---

## 2. How to Build

* **JDK Requirement:** JDK 17 (recommended: Android Studio's bundled JBR located at `<Android Studio>/jbr`).
* **Pinned Tooling & Dependency Versions:**
  - **Android Gradle Plugin (AGP):** `9.4.1`
  - **Kotlin:** `2.2.10`
  - **KSP:** `2.2.10-2.0.2`
  - **Room:** `2.8.4` (`room-runtime`, `room-ktx`, `room-compiler`, `room-testing`)
  - **Compile SDK:** 37
  - **Target SDK:** 37
  - **Min SDK:** 24
* **`gradle.properties` Setting:**
  - `android.disallowKotlinSourceSets=false` is set because AGP 9 disallows legacy Kotlin source set configurations by default; setting this to `false` permits KSP and Room generated schemas/sources to integrate cleanly.
* **Emulator Launching:**
  - Always start emulators via **Android Studio Device Manager** (with GUI enabled), **never** from the command line (`emulator -no-window`), as headless launches lack required GPU/SwiftShader buffers on Windows and crash.

---

## 3. How to Run Tests

* **Unit Tests:**
  ```powershell
  .\gradlew.bat testDebugUnitTest
  ```
  Runs fast JVM unit tests (`HashUtilTest`, etc.).
* **Instrumented Tests:**
  ```powershell
  .\gradlew.bat connectedDebugAndroidTest
  ```
  Run on an **EMULATOR only**. Do not run instrumented tests on a personal device containing real data, because Gradle test runners uninstall and reinstall the application APK, wiping the app's internal storage and Keystore entries.

---

## 4. Known Limitations (Carried to Stage 2)

* **No App Lock:** The vault list does not yet require biometric authentication or a PIN to open.
* **Exported Launcher:** `EvidenceListActivity` is currently the exported launcher screen for testing access.
* **Window Leakage:** `FLAG_SECURE` is not yet applied; snapshots in the Android Recents screen and notification shade are currently visible.
* **Best-Effort Source Label:** `sourceLabel` is inferred from caller activity intent metadata (`getCallingPackage()` or `EXTRA_REFERRER`).
* **Size vs. Hash Semantics:** `fileSizeBytes` reflects encrypted ciphertext size on disk, while `hash` is the SHA-256 of the unencrypted plaintext content.
* **Device Time:** `receiptTimeEpochMillis` is device-reported system time, not a cryptographically signed NTP or network timestamp.
* **Device-Local Only:** Cloud backup and device-to-device transfers are completely disabled; evidence exists strictly on the local device.
