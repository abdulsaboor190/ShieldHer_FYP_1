# Stage 3 Account Risk On-Device Privacy & Security Audit

Date: 2026-11-25
Branch: `fe/account-risk`
Scope: All files and code introduced or modified for on-device Account Risk under `app/src/main/java/com/shieldher/dashboard/check/`, `app/src/main/res/values/strings_risk.xml`, `app/src/main/res/layout/fragment_account_check.xml`, and associated assets.

---

## 1. Audit Invariants & Results Table

| # | Check Item | Target Scope | Requirement | Evidence / Findings | Result |
|---|---|---|---|---|:---:|
| 1 | Logging | `com.shieldher.dashboard.check.**` | Zero instances of `Log.`, `println`, `Timber`, or `printStackTrace` | `Get-ChildItem -Path app/src/main/java/com/shieldher/dashboard/check -Recurse -Filter *.kt \| Select-String -Pattern "Log\.", "println", "Timber", "printStackTrace"` returned 0 matches. | **PASS** |
| 2 | Persistence & Storage | `com.shieldher.dashboard.check.**` | No SharedPreferences, SQLite, Room, or file output writes of typed account data | `Select-String` for `SharedPreferences`, `openFileOutput`, `FileOutputStream`, `Room`, `getSharedPreferences`, `cacheDir`, `filesDir` returned 0 matches. User inputs exist in transient memory only. | **PASS** |
| 3 | Permissions | Merged Android Manifest | `android.permission.INTERNET` must be strictly absent | Manifest inspects show only Stage 1 permissions: `POST_NOTIFICATIONS` and `VIBRATE`. `INTERNET` permission is completely absent. | **PASS** |
| 4 | Network Stack | `com.shieldher.dashboard.check.**` | Zero network calls or HTTP client invocations | `Select-String` for `okhttp`, `HttpURLConnection`, `URL(`, `Socket`, `WebView` returned 0 matches in the Account Check feature. Evaluation runs 100% on-device CPU. | **PASS** |
| 5 | ViewModel & Lifecycle Privacy | `AccountCheckViewModel.kt`, `AccountCheckFragment.kt` | No `String` fields in ViewModel (reflection test); inputs and ViewModel state cleared on `onDestroyView` | `AccountCheckViewModelTest.kt:testViewModelHasNoStringFields` passes. `AccountCheckFragment.onDestroyView()` invokes `clearInputsAndResults()` and `viewModel.clear()`. Text fields set `saveEnabled = false`. | **PASS** |
| 6 | Forbidden Terminology | `strings_risk.xml`, `strings_check.xml` | None of: `fake`, `bot` (whole word), `malicious`, `verified`, `authentic`, `secure`, `protected`, `tamper`, `forged`, `%`, `probability` | Regex search for all forbidden whole words and `%`/`probability` returned 0 matches in both resource files. Wording strictly adheres to "resemblance to the research patterns". | **PASS** |
| 7 | Neutral Colour Tokens | `com.shieldher.dashboard.check.**`, layouts | No green/red status tokens (`sh_status_success_*`, `sh_tint_success`, `sh_status_error_*`, `sh_tint_error`, `sh_error`) | All risk badges use amber (`@color/sh_risk_amber` for Higher resemblance) or neutral grey (`@color/sh_risk_neutral` for Lower, Uncertain, Unsupported, Not Validated). Grep returned 0 forbidden color tokens. | **PASS** |
| 8 | Model Asset Integrity | `account_risk_model_v1.bin` | SHA-256 matches `docs/stage3/account_risk_model_v1.manifest.json` | Binary hash: `ad11c13741920a37702b8b2e82fabf147015e6e0f29b249bf979cf09228ccc55`. Matches manifest `sha256` exactly. Exported from frozen commit `c014a1264a5648a48ad59713910ff1c694606f87`. | **PASS** |

---

## 2. Summary
All 8 privacy and security invariants passed without exception. The on-device Account Check implementation does not leak, transmit, or persist user-typed profile data, uses zero network connectivity, displays non-accusatory neutral wording and colors, and deterministically executes the frozen Stage 2 model binary.
