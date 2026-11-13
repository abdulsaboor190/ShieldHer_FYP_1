# Commands

All commands are PowerShell, run in `D:\23i-3039\P2\ShieldHer1\Shieldher` unless stated:

## Build and test
- Build (does NOT install): `.\gradlew.bat assembleDebug`
- Unit tests (Stage 1 baseline: 7 pass): `.\gradlew.bat testDebugUnitTest`
- Build and unit test together: `.\gradlew.bat assembleDebug testDebugUnitTest`
- Lint: `.\gradlew.bat lintDebug`. NOTE: lintDebug currently fails because of 2 pre-existing Stage 1 errors in EvidenceRepository.kt (see docs/stage1_closeout.md). Use the error and warning counts only; do not try to fix them in Stage 3 work.
- Install on the connected device or emulator: `.\gradlew.bat installDebug`. Set `$env:ANDROID_SERIAL="<serial>"` first, otherwise it installs on EVERY connected device.
- Instrumented tests: `.\gradlew.bat connectedDebugAndroidTest` (EMULATOR ONLY, never a phone)
- List devices: `adb devices -l`

## Git workflow (repository: https://github.com/abdulsaboor190/ShieldHer_FYP_1.git)
- Start every task from a clean, updated main: `git switch main; git pull; git status --short` (must be empty).
- New branch: `git switch -c fe/<area>-<short-name>` (one deliverable per branch).
- Commit message starts with the deliverable ID, for example `FE-4.2a: evidence list with search`.
- Push and open a PR: `git push -u origin <branch>`; the author does not merge their own PR once branch protection is on.
- Never force-push. Never commit local.properties, keystores, build outputs or real evidence.

## Stage 2 (Abdul only, inside the ml folder with its virtual environment active)
- Freeze check: `python scripts/04_freeze.py verify` (must print OK)
- Never run the one-shot scoring scripts (05 and 06). Never open tweets.csv or crowdflower_results.csv.

## Safety rules (from Stage 1)
1. Start emulators from Android Studio only, never from the command line.
2. Run `connectedDebugAndroidTest` only on an emulator, never on a phone that holds test evidence.
3. `assembleDebug` does not install anything. Use `installDebug`.
4. Set ANDROID_SERIAL before `installDebug` when more than one device is connected.
5. Test phones hold no real evidence, ever.
6. No version upgrades before 10 Dec 2026 without all three members agreeing in writing on the PR.
7. After the 3 Dec 2026 freeze: fixes only.
