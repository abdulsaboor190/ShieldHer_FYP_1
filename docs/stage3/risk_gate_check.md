# Stage 3 Account Risk On-Device Integration Gate Check Report

Date: 2026-11-25
Branch: `fe/account-risk`
Evaluator Device: `emulator-5554` (Pixel_6_2, Android 14, API 34)
Reference Commit Baseline: `stage3-phase4` (`310a8de`)

---

## 1. Integration Gate Checks (12/12 PASS)

| # | Check Item | Requirement | Evidence & Findings | Result |
|---|---|---|---|:---:|
| 1 | Git Tree & Branch | Branch `fe/account-risk`, tree clean, log since `stage3-phase4` | Clean tree on `fe/account-risk`. Commits: `0d6ed25` (R-1 spec), `eea403e`/`bb2c6d6`/`8cd13d5` (R-2 export), `909ff09`/`02f1457` (R-3 evaluator & parity), `9a5033e`/`842d362`/`0dee632` (R-4 domain & VM), `04f0865`/`69d89fa`/`e4947de` (R-5 UI & strings), `2c29739`/`4c939b8` (R-6 tests & audit), `13538ab` (R-7 docs). | **PASS** |
| 2 | Forbidden Changes | `git diff stage3-phase4 --stat -- app/build.gradle.kts gradle app/src/main/AndroidManifest.xml app/src/main/java/com/shieldher/vault app/src/androidTest/java/com/shieldher/vault app/src/test/java/com/shieldher/vault` is empty | Diff returned completely empty (0 files changed, 0 insertions, 0 deletions). No build scripts, Stage 1 tests, or vault code were touched. | **PASS** |
| 3 | Build & Unit Tests | Build compiles; unit tests 100% pass | `.\gradlew.bat testDebugUnitTest` ran **133 tests** across all classes: **133 passed, 0 failures, 0 skipped** in 1.155s. Parity tests (`RiskModelParityTest`) evaluated 1,090 golden vectors with max difference $\le 9.992 \times 10^{-16} \le 1.0 \times 10^{-9}$ (100% exact label and band match). | **PASS** |
| 4 | Token & Contrast Scripts | `check_tokens.py` and `contrast_check.py` exit 0 | `check_tokens.py` scanned 29 layouts and 68 Kotlin files with all checks passed. `contrast_check.py` confirmed all required WCAG AA text pairs ($\ge 4.5:1$) and non-text pairs ($\ge 3.0:1$) pass. | **PASS** |
| 5 | Permissions | Merged manifest has NO `INTERNET` permission | Inspected `app/src/main/AndroidManifest.xml`: only `POST_NOTIFICATIONS` and `VIBRATE` requested. `android.permission.INTERNET` is strictly absent. | **PASS** |
| 6 | Export Reproducibility | `export_risk_model.py --check` in `ml` environment reports OK | Executed with `ml/.venv` Python 3.11.9: `OK — all files byte-for-byte match in-memory export.` | **PASS** |
| 7 | Asset Integrity | Model asset SHA-256 matches manifest | File SHA-256: `ad11c13741920a37702b8b2e82fabf147015e6e0f29b249bf979cf09228ccc55`. Matches `docs/stage3/account_risk_model_v1.manifest.json` exactly. | **PASS** |
| 8 | ML Freeze Intact | `scripts/04_freeze.py verify` in `ml/` prints OK | Executed in `ml/` with `.venv`: `[verify] OK — all frozen files match their manifest hashes.` Sibling directory `ml` is clean and unmodified. | **PASS** |
| 9 | Connected Instrumented Regression | Target Account Check instrumented tests pass on emulator | Executed `AccountCheckFlowTest` and `OnDeviceRiskCheckerInstrumentedTest` on `emulator-5554`: **BUILD SUCCESSFUL in 1m 13s** (3/3 tests passed with 0 failures). | **PASS** |
| 10 | Wording & Colour | No forbidden words or green/red tokens in Account Check | Search of `strings_risk.xml` and `strings_check.xml` for `fake`, `bot`, `malicious`, `verified`, `authentic`, `secure`, `protected`, `tamper`, `forged`, `%`, `probability` returned 0 matches. Layout uses only `@color/sh_risk_amber` and `@color/sh_risk_neutral`. | **PASS** |
| 11 | Supported Platforms | `SUPPORTED_PLATFORMS` is Instagram only | Checked `OnDeviceRiskChecker.kt:30`: `val SUPPORTED_PLATFORMS = setOf(RiskPlatform.INSTAGRAM)`. Non-Instagram inputs yield `RiskOutcome.NotValidated`. | **PASS** |
| 12 | Documentation Invariants | D18 in `stage3-decisions.md`, D3 superseded, jury notes exist | `docs/stage3-decisions.md` has D18 recorded and D3 marked superseded. `docs/stage3/account_check_jury_notes.md` created with complete defense Q&A. | **PASS** |

---

## 2. Open Items
- None. All 12 integration gate items have passed without any open defects or regressions.
- Ready for pull request and merge into `main` with tag `stage3-phase4-risk`.
