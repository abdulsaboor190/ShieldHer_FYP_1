# Tech Register

Date: 2026-11-13. Technology baseline and tool register for ShieldHer Stage 3.

| Technology | Purpose | Needed by | Version | Verify with | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Android Studio | IDE and emulator manager | All | [fill in - each member, Help > About] | Opens the project and Gradle sync succeeds | All |
| JDK | Builds the project | All | 21.0.7 on this machine (AGP 9.4.1 supports it; all three machines should use the same major version) | `java -version` | All |
| Android SDK: compileSdk 37, targetSdk 37, minSdk 24; platform android-37.0; build-tools 36.0.0; platform-tools (adb 1.0.41, version 37.0.1-15733141) | Build and adb | All | compileSdk 37, targetSdk 37, minSdk 24; platform android-37.0; build-tools 36.0.0; platform-tools adb 1.0.41 (version 37.0.1-15733141) | `adb version`; SDK Manager lists the packages | All |
| Emulator system images (one Android 12+ and one older, not below API 24) | Testing | All | [fill in - Gotam] | Both start from Android Studio | Gotam |
| Gradle wrapper (do not install Gradle separately) | Reproducible builds | All | 9.6.0 | `.\gradlew.bat --version` | Gotam |
| Android Gradle Plugin | Build | All | 9.4.1 | matches this register | Gotam |
| Kotlin (compiler) | Language | All | 2.2.10 (note: Gradle's embedded Kotlin 2.3.21 is for build scripts only) | compiles | Gotam |
| KSP | Room annotation processing | All | 2.2.10-2.0.2 | compiles | Gotam |
| Room | Vault metadata DB (no migration this stage) | Existing | 2.8.4 | Stage 1 tests pass | Bhoomika |
| Jetpack Security (security-crypto) | Vault files and the case store | Existing and new | 1.1.0-alpha06; deprecation status "unknown, check at FE-0.2" unless the build output clearly says otherwise | dependency resolves | Bhoomika |
| Material Components (Material 3) | Theme and components | Already present | 1.10.0 (already in gradle/libs.versions.toml; keep it under the freeze rule unless FE-0.2 shows a newer version is needed) | an M3 component renders | Gotam |
| Navigation (fragment and UI), no Safe Args | Screen navigation | New | not found yet; added in FE-0.2 | empty fragment navigates | Gotam |
| fragment-ktx, lifecycle (ViewModel and flow support) | Screens and state | New or existing | [fill in - FE-0.2] | compiles | Gotam |
| AndroidX Test (ext:junit 1.1.5) and Espresso (3.5.1) | UI and instrumented tests | Existing | ext:junit 1.1.5, Espresso 3.5.1 | Stage 1 instrumented tests run on an emulator | Abdul |
| kotlinx-coroutines-test, arch-core-testing | ViewModel unit tests | New | [fill in - FE-0.2] | template test passes (FE-3.6) | Gotam |
| Git and GitHub (repository ShieldHer_FYP_1) | Source control | All | [fill in - each member] | push to a branch works | Abdul |
| Python 3.11.9 and the pinned Stage 2 packages (scikit-learn 1.9.1, pandas 3.0.6, numpy 2.4.6, shap 0.51.0) | Jury-slide results; later the service | Abdul | Python 3.11.9, scikit-learn 1.9.1, pandas 3.0.6, numpy 2.4.6, shap 0.51.0 | `python scripts/04_freeze.py verify` prints OK (run inside the ml folder) | Abdul |
| Accessibility Scanner (Play Store) | Contrast and touch-target checks | All | [fill in] | installed on the test phones and runs | All |
| Wireframe tool: paper or free Figma | Wireframes | Gotam | [fill in - decision in FE-E.10] | decision recorded | Gotam |
| Vector Asset Studio (inside Android Studio) | Material icons as vector drawables | Gotam | part of Android Studio | one icon imported (FE-E.10) | Gotam |
| System font (no custom font) | Typography | All | n/a | decision recorded (FE-E.10) | Gotam |

## Known issues
Stage 1 lint has 2 errors in EvidenceRepository.kt (see docs/stage1_closeout.md); `android.disallowKotlinSourceSets=false` is an existing experimental flag the build relies on (do not upgrade AGP or KSP before 10 Dec).

## Not needed before January
Docker, FastAPI and uvicorn (Account Check service, FE-7.1), androidx.biometric (only if app lock is wanted), SQLCipher (not used, decision D7).
