# Phase 4 State Audit: Screens 1 to 7 (FE-4.12)

**Audit Date:** 24 November 2026  
**Auditor:** Automated Codebase Audit (Gotam lane)  
**Branch:** `fe/p4-state-audit`  
**Repository:** https://github.com/abdulsaboor190/ShieldHer_FYP_1.git  

---

## 1. Screen State Audit (Screens 1 to 7)

Audit covering each required state across screens 1 through 7:
- Loading, Empty, Content, Error states on every screen
- Specific screen-level states per prompt P4-12 specification

| Screen | Required State | Implementation File & Class | Status | Notes |
|---|---|---|---|---|
| **1. Home** | Loading | `HomeFragment.kt`, `HomeViewModel.kt` | **PASS** | Renders `loading_indicator` when state is `UiState.Loading` |
| 1. Home | Empty | `HomeFragment.kt`, `HomeViewModel.kt` | **PASS** | Renders `empty_view` with `EmptyStateView` |
| 1. Home | Content | `HomeFragment.kt`, `HomeViewModel.kt` | **PASS** | Displays `content_scroll_view` with evidence & case stats and latest cards |
| 1. Home | Error | `HomeFragment.kt`, `HomeViewModel.kt` | **PASS** | Renders `error_view` with `ErrorStateView` and retry callback |
| **2. Evidence list** | Loading | `EvidenceListDashboardFragment.kt`, `EvidenceListDashboardViewModel.kt` | **PASS** | Renders `loading_indicator` |
| 2. Evidence list | Empty | `EvidenceListDashboardFragment.kt`, `EvidenceListDashboardViewModel.kt` | **PASS** | Renders `empty_view` with import action button |
| 2. Evidence list | Content | `EvidenceListDashboardFragment.kt`, `EvidenceListDashboardViewModel.kt` | **PASS** | Renders `recycler_evidence` with item count and badges |
| 2. Evidence list | Error | `EvidenceListDashboardFragment.kt`, `EvidenceListDashboardViewModel.kt` | **PASS** | Renders `error_view` with retry callback |
| 2. Evidence list | No-match search | `EvidenceListDashboardFragment.kt`, `EvidenceListDashboardViewModel.kt` | **PASS** | Renders empty state with search-specific wording when filter returns 0 results |
| **3. Evidence detail** | Loading | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Renders `loading_indicator` |
| 3. Evidence detail | Empty | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **N/A** | Detail screen displays single item; empty state handled via `evidence not found` error |
| 3. Evidence detail | Content | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Full detail content scroll view with metadata, verification and actions |
| 3. Evidence detail | Error / Not found | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Shows error view when item ID cannot be retrieved or found |
| 3. Evidence detail | Not checked | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Initial verification state displays `status_not_checked` badge |
| 3. Evidence detail | Unchanged | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Successful verification shows `status_unchanged` badge |
| 3. Evidence detail | Changed | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Verification mismatch shows `status_changed` badge |
| 3. Evidence detail | Missing | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Missing file shows `status_missing` badge |
| 3. Evidence detail | Preview unavailable | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Displays fallback indicator when preview unavailable |
| 3. Evidence detail | Preview too large | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Displays oversized warning message and placeholder |
| 3. Evidence detail | Preview unsupported | `EvidenceDetailFragment.kt`, `EvidenceDetailViewModel.kt` | **PASS** | Displays generic unsupported file type preview |
| 3. Evidence detail | Delete confirmation | `EvidenceDetailFragment.kt`, `ConfirmDialog.kt` | **PASS** | Destructive confirmation dialog before deleting vault evidence |
| **4. Import** | Loading / Saving | `ImportFragment.kt`, `ImportViewModel.kt` | **PASS** | Shows progress bar and status text during import hashing/saving |
| 4. Import | Empty / Idle (picking) | `ImportFragment.kt`, `ImportViewModel.kt` | **PASS** | Initial state prompting user to select file via SAF |
| 4. Import | Content / Saved | `ImportFragment.kt`, `ImportViewModel.kt` | **PASS** | Shows success confirmation card with `View evidence` button |
| 4. Import | Error: Too large | `ImportFragment.kt`, `ImportViewModel.kt` | **PASS** | Error view showing file size limit exceeded message |
| 4. Import | Error: Unsupported | `ImportFragment.kt`, `ImportViewModel.kt` | **PASS** | Error view showing unsupported MIME type message |
| 4. Import | Error: Failed | `ImportFragment.kt`, `ImportViewModel.kt` | **PASS** | Error view with generic read/storage failure and retry |
| **5. Cases list** | Loading | `CasesListFragment.kt`, `CasesListViewModel.kt` | **PASS** | Shows `progress_loading` indicator |
| 5. Cases list | Empty | `CasesListFragment.kt`, `CasesListViewModel.kt` | **PASS** | Shows `empty_state_view` with `Create case` prompt |
| 5. Cases list | Content | `CasesListFragment.kt`, `CasesListViewModel.kt` | **PASS** | Shows `recycler_cases` list with popup menu options |
| 5. Cases list | Error | `CasesListFragment.kt`, `CasesListViewModel.kt` | **PASS** | Shows `error_state_view` with retry callback |
| 5. Cases list | Unreadable store | `CasesListFragment.kt`, `CasesListViewModel.kt` | **PASS** | Handles `CaseStoreException` with warning banner / error state |
| **6. Case timeline** | Loading | `CaseTimelineFragment.kt`, `CaseTimelineViewModel.kt` | **PASS** | Shows `progress_loading` indicator |
| 6. Case timeline | Empty case | `CaseTimelineFragment.kt`, `CaseTimelineViewModel.kt` | **PASS** | Shows `empty_state_view` with actions to add evidence or note |
| 6. Case timeline | Content | `CaseTimelineFragment.kt`, `CaseTimelineViewModel.kt` | **PASS** | Displays chronological timeline list with evidence and note cards |
| 6. Case timeline | Error | `CaseTimelineFragment.kt`, `CaseTimelineViewModel.kt` | **PASS** | Shows `error_state_view` with retry callback |
| 6. Case timeline | "Evidence deleted" item | `CaseTimelineFragment.kt`, `item_timeline_deleted.xml` | **PASS** | Grey card showing preserved note and incident date when evidenceId is removed |
| 6. Case timeline | Unreadable store | `CaseTimelineFragment.kt`, `CaseTimelineViewModel.kt` | **PASS** | Displays error message when case store is corrupted or unreadable |
| **7. Settings & privacy** | Loading | `SettingsFragment.kt`, `SettingsViewModel.kt` | **PASS** | Handled via initial StateFlow state |
| 7. Settings & privacy | Empty | `SettingsFragment.kt`, `SettingsViewModel.kt` | **N/A** | Settings screen always renders static configuration items |
| 7. Settings & privacy | Content | `SettingsFragment.kt`, `SettingsViewModel.kt` | **PASS** | Shows evidence count, screenshot switch, limits link, delete all |
| 7. Settings & privacy | Error | `SettingsFragment.kt`, `SettingsViewModel.kt` | **PASS** | Displays error view if count loading or deletion fails |
| 7. Settings & privacy | Delete-all confirmation | `SettingsFragment.kt`, `ConfirmDialog.kt` | **PASS** | Destructive confirm dialog warning that all records will be deleted |
| 7. Settings & privacy | Delete-all failure message | `SettingsFragment.kt`, `SettingsViewModel.kt` | **PASS** | Error toast / state message when deletion operation fails |

---

## 2. Wording Rules & Prohibited Terms Audit

### Forbidden Words Check
Searched across all string resource files (`app/src/main/res/values/strings*.xml`) for the prohibited words: `verified`, `authentic`, `secure`, `protected`, `tampered`, `forged`, `fake`, `bot`.

- **Matches for `verified`:** 0
- **Matches for `authentic`:** 0
- **Matches for `tampered`:** 0
- **Matches for `forged`:** 0
- **Matches for `fake`:** 0
- **Matches for `bot`:** 0
- **Matches for `secure`:** 0
- **Matches for `protected` / `protection`:** 2
  - `strings_settings.xml:7`: `<string name="settings_section_screenshot">Screenshot protection</string>` (Technical Android OS feature name per D8)
  - `strings_onboarding.xml:31`: `Screenshot protection is enabled by default to prevent other apps or previews from capturing your screen.` (Technical setting description per D8)

### PROPOSED Comments Check
Every sentence in all new strings files carries the required comment:
`<!-- PROPOSED: needs Abdul's review -->`

Files audited:
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/strings_cases_list.xml`
- `app/src/main/res/values/strings_case_timeline.xml`
- `app/src/main/res/values/strings_check.xml`
- `app/src/main/res/values/strings_detail.xml`
- `app/src/main/res/values/strings_detail_actions.xml`
- `app/src/main/res/values/strings_evidence_list.xml`
- `app/src/main/res/values/strings_home.xml`
- `app/src/main/res/values/strings_import.xml`
- `app/src/main/res/values/strings_nav.xml`
- `app/src/main/res/values/strings_onboarding.xml`
- `app/src/main/res/values/strings_settings.xml`

All user-facing strings are strictly proposed and await Abdul's final review.

---

## 3. Color & Token Audit

- **Green Tokens (`sh_status_success_*`):**
  - Layouts & styles inspected: verified that green status colors appear **only** in `StatusBadge` and verification status indicators. No general cards, buttons, or backgrounds use success green.
- **`sh_accent` Token:**
  - Searched across all layouts, styles, and Kotlin files.
  - Usage count: **0** (completely absent, strictly adhering to design token specifications).
- **Hardcoding Check:**
  - Automated check `scripts/check_tokens.py` passed with 0 errors across 29 dashboard layouts and 60 Kotlin files.

---

## 4. Privacy & Data Handling Audit

- **Logging / Debug Prints:**
  - Searched for `Log.`, `println`, `printStackTrace` in `com.shieldher.dashboard.*`.
  - Found: **0** occurrences.
- **Disk Leaks:**
  - Decrypted content is never cached or written to temporary or external storage.
  - Previews for text read directly into memory strings with strict byte caps (`maxBytes = 32768`).
- **Input Fields Privacy:**
  - All input fields (`TextInputEditText`, `AutoCompleteTextView`) set:
    - `android:saveEnabled="false"`
    - `android:inputType="...|textNoSuggestions"`
    - `android:importantForAutofill="no"`
  - Prevents keyboard caching, dictionary suggestions, OS autofill, and Android bundle saved state leakage.
- **Screen Protection:**
  - `WindowManager.LayoutParams.FLAG_SECURE` applied before `setContentView` in `MainActivity.kt`.

---

## 5. Open Items

1. **PROPOSED Strings Review:** Abdul to review and sign off on all strings tagged with `<!-- PROPOSED: needs Abdul's review -->`.
2. **Espresso on API 37:** Espresso 3.5.1 uses reflection on `android.hardware.input.InputManager.getInstance()` which was removed in Android 17 / API 37. Run instrumented test suite on an Android 12–15 emulator (API 31–34) for full test pass.

---

## Summary
- **Total Screens Audited:** 7 (Home, Evidence list, Evidence detail, Import, Cases list, Case timeline, Settings)
- **State Audit Status:** **ALL PASS** (0 FAIL rows)
- **Token Check:** **PASS** (0 hardcoded values)
- **Privacy Audit:** **PASS** (0 logging or cache leaks)
