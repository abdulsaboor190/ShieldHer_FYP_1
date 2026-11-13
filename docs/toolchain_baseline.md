# Toolchain baseline
Date: 2026-11-13

## Freeze rule
"No version upgrades (Android Gradle Plugin, Gradle, Kotlin, KSP, Room, Material, Navigation, or any dependency) before Thursday 10 December 2026, unless all three team members agree in writing on the PR."

## Versions
| Item | Version | Source File |
| :--- | :--- | :--- |
| Android Gradle Plugin (AGP) | 9.4.1 | gradle/libs.versions.toml |
| Gradle | 9.6.0 | gradle/wrapper/gradle-wrapper.properties |
| Kotlin (Project / Compiler) | 2.2.10 | kotlinCompilerClasspath (org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.10). Note: Gradle's embedded Kotlin (2.3.21) is for build scripts only. |
| KSP | 2.2.10-2.0.2 | app/build.gradle.kts |
| Room | 2.8.4 | app/build.gradle.kts |
| Jetpack Security (security-crypto) | 1.1.0-alpha06 | app/build.gradle.kts |
| AndroidX Test (ext:junit) | 1.1.5 | app/build.gradle.kts (also libs.versions.toml) |
| AndroidX Espresso | 3.5.1 | app/build.gradle.kts (also libs.versions.toml) |
| compileSdk | 37 (release 37) | app/build.gradle.kts |
| minSdk | 24 | app/build.gradle.kts |
| targetSdk | 37 | app/build.gradle.kts |
| namespace | com.shieldher | app/build.gradle.kts |
| Navigation | not found | app/build.gradle.kts (to be added in FE-0.2) |
| Material Components | 1.10.0 | gradle/libs.versions.toml (already present; kept under freeze rule unless FE-0.2 shows a newer version is needed) |

## This machine
Machine: Gotam/Bhoomika/Abdul: [fill in who ran this]
- **JDK:** 21.0.7 (Oracle Corporation 21.0.7+8-LTS-245; works with AGP 9.4.1; JAVA_HOME is unset, java resolves from PATH).
- **Gradle:** 9.6.0
- **adb:** Android Debug Bridge version 1.0.41 (Version 37.0.1-15733141)
- **SDK Platforms installed:** android-37.0
- **SDK Build-tools installed:** 36.0.0

## Experimental Flag & Build Constraints
android.disallowKotlinSourceSets=false in gradle.properties is an existing experimental flag the build relies on. Do not upgrade AGP or KSP before 10 Dec.

## Stage 1 test baseline
- **assembleDebug:** BUILD SUCCESSFUL
- **Unit tests passed:** 7 passed (0 failures, 0 errors, 0 skipped across ExampleUnitTest and HashUtilTest)

## Lint baseline
- **Errors:** 2 errors (located in Stage 1 EvidenceRepository.kt: @VisibleForTesting support annotation usage)
- **Warnings:** 35 warnings
- *Note:* lintDebug aborted build due to the 2 errors found in existing Stage 1 code; no baseline file created per instructions.

## Still to fill in by a human
- Android Studio version and build number (Help > About)
- Navigation version (added later in FE-0.2)
- Other machines (see docs/machines/ after FE-E.2)
