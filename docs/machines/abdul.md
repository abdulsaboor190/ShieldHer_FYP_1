# Machine Check: Abdul (Android Environment)

- **Date:** 2026-11-16
- **Developer:** Abdul Saboor
- **Role:** FYP-I Stage 3 Lead

## Operating System & Toolchain

| Tool / Environment | Version / Detail |
| :--- | :--- |
| **OS** | Microsoft Windows 11 Pro (Build 26200) |
| **Architecture** | x86_64 / amd64 |
| **Git** | 2.51.0.windows.1 |
| **JDK** | Oracle Java SE 21.0.7 LTS (build 21.0.7+8-LTS-245) |
| **Gradle** | 9.6.0 (embedded Kotlin 2.3.21) |
| **Android Gradle Plugin** | 8.8.1 (AGP) |
| **Kotlin Compiler** | 2.2.10 |
| **Android SDK Platform** | android-37.0 (API 37) |
| **Build Tools** | 36.0.0 |
| **ADB** | 1.0.41 (version 37.0.1-15733141) |

## Clean Clone Verification

Proven by cloning the published GitHub repository (`https://github.com/abdulsaboor190/ShieldHer_FYP_1.git`) into an isolated temporary directory with a clean `local.properties`:

- `.\gradlew.bat assembleDebug`: **BUILD SUCCESSFUL** (34s, 39 actionable tasks executed)
- `.\gradlew.bat testDebugUnitTest`: **BUILD SUCCESSFUL** (8s, all unit tests passed)

No stray local settings or uncommitted dependencies required to build from scratch.
