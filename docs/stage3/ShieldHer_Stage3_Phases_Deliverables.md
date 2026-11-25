# ShieldHer — Stage 3 (Dashboard / Front-End): Phases and Deliverables

**Written:** 12 November 2026
**Hard dates:** feature freeze **Thu 3 Dec 2026**, submission **Thu 10 Dec 2026**. After the freeze: fixes, testing and rehearsal only.
**Decision record:** `ShieldHer_Stage3_Dashboard_Plan_v2.md` (decisions D1 to D17, architecture, risks). This file is the working breakdown. It keeps the v2 deliverable IDs (so references like FE-1.3 and FE-7.5 in the design basis still work) and splits big items with letter suffixes (FE-4.3a, FE-4.3b, …). Each row is sized for **one agent prompt** with a single output and a single check. **Phase E (environment and project setup) is new: its IDs are FE-E.1 to FE-E.12 and it comes first.**
**Design basis:** the palette, contrast and component rules you supplied, reviewed in section 0 below.

---

## 0. Design basis review (do this before any colour goes into code)

I recomputed every contrast ratio in your table with the WCAG formula. **All the figures in your table are correct.** The checks you did not run turn up six problems. Fix them in FE-1.3 so they do not reach the app.

| # | Finding | Numbers | Fix |
|---|---|---|---|
| 1 | **Soft turquoise #55A6A0 fails the 3:1 rule for non-text elements** on every surface, yet the basis uses it for progress indicators and selected-state accents, and lists it as a loading-indicator option. | 2.9 on white, 2.7 on off-white, 2.4 on pale mint | Decoration only. Loading indicators and selected states use **ocean teal** (5.2 on white, 4.95 on off-white). |
| 2 | **Amber and grey fill colours fail 3:1 as icons.** | Amber 2.3 on off-white, 2.1 on pale mint. Grey 3.2 on off-white, 2.8 on pale mint | Badge icons use the **text version** colour of each semantic (#2F6F5A, #8F5E12, #666A72, #AD464E), all of which pass. Fill colours are for tinted backgrounds only. |
| 3 | **Saturated fills with white text fail.** | White on red fill 4.2, amber 2.5, grey 3.3 (green fill 4.5 only just passes) | Badges use **light tints** with text-version text and icon. Candidate tints, already checked: success #E6F2EE (text 5.2), amber #FBF1E0 (5.0), grey #ECEDEF (4.6), red #F8E8E9 (4.7). Only the destructive button keeps a filled red (#AD464E with white, 5.6). |
| 4 | **Secondary text and ocean teal on warm mint fail.** | Secondary text 3.7, ocean teal 3.2 | On warm mint use only main text (7.8) or deep teal (5.8). Ocean teal text on pale mint is 4.44, also below 4.5, so use deep teal there (your table already says this). |
| 5 | **The Material 3 mapping is incomplete.** It omits outline, surface-container, inverse and tertiary roles, so Material's default (purple-tinted) values would leak into dialogs, menus and text fields. | n/a | FE-3.1b fills every role, and the gallery shows each component to catch leaks. |
| 6 | **Dynamic colour and dark mode.** Dynamic colour is opt-in in Material Components, so the rule is simply *never call* `DynamicColors.applyToActivitiesIfAvailable`. The basis defines a light palette only; with a DayNight parent, a phone in dark mode would show a half-dark app. | n/a | Use a Light M3 parent and force light mode. Decision DS-3. |

Also: the basis says EmptyState uses a "calm illustration or icon". There is no time to produce illustrations. Use Material vector icons only.

### Design decisions to confirm (record in `docs/stage3-decisions.md`)

| ID | Question | My recommendation | Owner | By |
|---|---|---|---|---|
| DS-1 | Colour of "Changed or unreadable since saved" | **Muted red** with a warning icon, a different icon from "File missing". Reserve amber for "uncertain / requires review" (Account Check, FYP-II). Amber is the alternative if the team wants it softer, since the cause may be file corruption rather than tampering. | Team | 14 Nov |
| DS-2 | Badge style | Tinted background + text-version text and icon (finding 3) | Gotam | 14 Nov |
| DS-3 | Theme | Light only, forced; no dynamic colour | Gotam | 14 Nov |
| DS-4 | Icon set | Material vector icons (Apache 2.0), listed in FE-1.3b | Gotam | 15 Nov |

---

## 1. Overview

| Phase | Dates | Depends on | Size | Core question |
|---|---|---|---|---|
| PE Environment and project setup | 12–14 Nov | — | Small–Medium | Does every machine, device and the repo work before we write code? |
| P0 Confirm and spike | 12–15 Nov | PE (for FE-0.2, FE-0.3) | Small | Do the decisions and the toolchain hold? |
| P1 Design | 12–17 Nov | — | Small–Medium | Are the look, wording and screen list settled and accessible? |
| P2 Vault additions and case store | 13–22 Nov | P0 (FE-0.3, FE-0.6) | Medium | Can the UI get what it needs without touching Stage 1? |
| P3 Shell and design system in code | 16–24 Nov | P0 (FE-0.2), P1 (FE-1.3) | Medium | Does the app have one consistent look and navigation? |
| P4 Screens | 20 Nov – 1 Dec | P2, P3 | Large | Can a person do the core flow end to end? |
| P5 Demo, test, freeze | 30 Nov – 3 Dec | P4 | Small–Medium | Is it demonstrable and safe to freeze? |
| P6 Fixes, slides, rehearsal | 4–10 Dec | P5 | Small–Medium | Are we ready to submit? |
| P7 FYP-II build | 4 Jan – 26 Feb | P6 | Large | Is every screen complete and accessible? |
| P8 Hardening | 1–26 Mar | P7 | Medium | Is it private and robust? |
| P9 Usability study | 1–23 Apr | P8 + ethics | Medium | Do people understand it, and what it does not prove? |
| P10 Final | 26 Apr – May | P9 | Small–Medium | Is it documented and defensible? |

**Parallel tracks:** PE and the decisions in P0 start together; P1 and P2 run side by side. P3 starts on 16 Nov using the colour starter, then picks up the final tokens on 17 Nov.

**Checkpoints:** 14 Nov (Phase E done: all three machines build the repo and pass the Stage 1 unit tests), 22 Nov (P2 done, shell navigates) and 27 Nov (core flow works end to end). If 22 or 27 Nov is missed, apply the cut order.

**Cut order for FYP-I (first to go):** FE-4.10 Help → FE-4.9 Account Check layout → FE-4.8 Onboarding → FE-5.2 tamper tool → polish of Settings. **Never cut:** FE-4.2 to FE-4.6, the Verify button, demo mode, Settings delete-all and the limits page.

---

## Phase E — Environment and Project Setup (Thu 12 – Sat 14 Nov)

**Depends on:** nothing.
**Size:** Small–Medium. Mostly verification, because Stage 1 already left a working project.
**What gets built:** a written technology register; three developer machines that build the project; two emulators and two real test phones; a protected repository with a branch rule and a task board; the target folder structure; the two Gradle flags this plan needs; a recorded toolchain baseline; the ML environment for the jury slides; and a one-page commands and safety sheet.
**Why its own phase:** the same reason as Stage 1 Phase 0. A machine or dependency problem found on 25 Nov looks like a code bug. This plan also needs `BuildConfig` and view binding, which are off by default in current Android Gradle Plugin versions. If `BuildConfig.DEBUG` is missing, demo mode and the tamper tool fail late.

**Acceptance criteria**
- [ ] Each of the three members has run `assembleDebug` and `testDebugUnitTest` on their own machine, from a fresh clone.
- [ ] Two emulators and two real phones run the debug app.
- [ ] `main` needs a reviewed pull request; all three members can push branches.
- [ ] `BuildConfig.DEBUG` is true in debug and false in release (checked).
- [ ] The toolchain versions are written down and frozen until 10 Dec.
- [ ] Abdul's ML environment passes the Stage 2 freeze verification.

### Technology register (the content of FE-E.1)

Fill in the versions from the Stage 1 and Stage 2 records. Anything marked **[fill in]** is a version the register must record, not guess.

| Technology | Purpose | Needed by | Verify with |
|---|---|---|---|
| Android Studio (the version Stage 1 used) | IDE, emulator manager | All | Opens the project; Gradle sync succeeds |
| JDK 17 | Builds the project | All | `java -version` shows 17 |
| Android SDK: compile and target as in Stage 1 (min SDK 24), build-tools, platform-tools | Build and `adb` | All | `adb version` works; SDK Manager shows the packages |
| Emulator system images: one Android 12+ image and one older image | Testing, including the dynamic-colour check | All | Both start from Android Studio |
| Gradle wrapper (do **not** install Gradle separately) | Reproducible builds | All | `.\gradlew.bat --version` |
| Android Gradle Plugin 9.4.1 (as recorded in Stage 1) | Build | All | Matches the register |
| Kotlin and KSP versions | Language and Room processing | All | **[fill in]** from the Stage 1 build files |
| Room 2.8.4 (existing; **no migration** this stage) | Vault metadata DB | Existing | Stage 1 tests pass |
| Jetpack Security (`security-crypto`) | Vault files and case store | Existing + new use | Resolves; deprecation status noted (FE-0.2) |
| Material Components (Material 3) | Theme and components | New | **[fill in]** at FE-0.2: latest stable compatible with the AGP above |
| Navigation (fragment and UI), **no Safe Args** | Screen navigation | New | Empty fragment navigates |
| fragment-ktx, lifecycle (ViewModel and flow support) | Screens and state | New / existing | Compiles |
| AndroidX Test and Espresso (existing) | UI and instrumented tests | Existing | Stage 1 instrumented tests run on an emulator |
| kotlinx-coroutines-test, arch-core-testing | ViewModel unit tests | New | Template test passes (FE-3.6) |
| Git, GitHub access | Source control | All | Push to a branch works |
| Python 3.11.9 and the pinned Stage 2 packages (scikit-learn 1.9.1, pandas 3.0.6, numpy 2.4.6, shap 0.51.0) | Jury-slide results, later the service | Abdul (and anyone running `docs/contrast_check.py`, which needs only standard Python 3) | `python scripts/04_freeze.py verify` prints OK |
| Accessibility Scanner (Play Store) on the test phones | Contrast and touch-target checks | All | Installed and runs |
| Wireframe tool: paper or free Figma | Wireframes | Gotam | Decision recorded |
| Vector Asset Studio (inside Android Studio) | Material icons as vector drawables | Gotam | One icon imported |
| System font (no custom font) | Typography | All | Decision recorded: no download, fewer licensing and loading problems |

**Not needed before January:** Docker, FastAPI and uvicorn (Account Check service, FE-7.1), `androidx.biometric` (only if app lock is wanted), SQLCipher (not used, decision D7).

### Target project structure (the content of FE-E.6)

Assumes one repository holding the Android app and `ml/`. If the Android app lives in a separate repository, adjust the root.

```
repo root
├─ app/
│  ├─ schemas/                         (Room schema JSON, existing)
│  └─ src/
│     ├─ main/
│     │  ├─ java/com/shieldher/
│     │  │  ├─ vault/                  (Stage 1: add code only)
│     │  │  └─ dashboard/
│     │  │     ├─ common/              StatusBadge, EvidenceCard, states, dialogs
│     │  │     ├─ home/  evidence/  cases/  settings/
│     │  │     ├─ help/  onboarding/  check/
│     │  │     └─ debug/               demo seeder, tamper tool, gallery (debug only)
│     │  └─ res/
│     │     ├─ values/                 colors, themes, dimens, styles, strings
│     │     ├─ drawable/               ic_status_*.xml, nav icons
│     │     ├─ layout/  navigation/
│     ├─ test/                         unit tests
│     └─ androidTest/                  instrumented and Espresso tests
├─ ml/                                 (Stage 2, untouched)
├─ docs/
│  ├─ stage3-decisions.md  design_basis.md  wording_guide.md
│  ├─ stage1_closeout.md  proposal_deviations.md  toolchain_baseline.md
│  ├─ tech_register.md  test_devices.md  commands.md
│  ├─ contrast_check.py  wireframes/
└─ scripts/                            check_tokens.sh
```

Debug-only code lives under `dashboard/debug/` and is compiled out of release builds (FE-8.5 checks this).

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-E.1 | Technology register | `docs/tech_register.md` from the table above | Every row has a version or an explicit "[fill in]" with an owner; each has a verify command | Bhoomika |
| FE-E.2 | Developer machines | One checklist per member, committed in `docs/toolchain_baseline.md` | Each member, from a fresh clone, runs `.\gradlew.bat assembleDebug testDebugUnitTest` with zero errors; JDK, SDK and `adb` versions recorded per machine | Each member |
| FE-E.3 | Emulators | Two AVDs recorded in `docs/test_devices.md` | An Android 12+ image and an older image both start **from Android Studio** and run the debug app | Gotam |
| FE-E.4 | Real test phones | Entries in `docs/test_devices.md` | Two phones (different manufacturers if possible) with Developer options and USB debugging on, Accessibility Scanner installed, `installDebug` works; each holds **no real evidence** | Bhoomika |
| FE-E.5 | Repository and branching | Repo settings, `.gitignore`, PR template | `main` requires one reviewed pull request; branch names `fe/<area>-<short-name>`; PR template carries the standing-rules checklist; `local.properties`, build outputs and any keystore files are ignored; all three can push | Bhoomika |
| FE-E.6 | Project structure | Empty folders, packages and resource files from the tree above | App still builds and launches; Stage 1 files unchanged (`git diff` shows additions only) | Gotam |
| FE-E.7 | Gradle baseline flags | Updated Gradle files | `buildFeatures.viewBinding = true` and `buildConfig = true`; version catalog (`gradle/libs.versions.toml`) used for versions; `BuildConfig.DEBUG` is true in debug and false in release (checked with a throwaway log or test) | Gotam |
| FE-E.8 | Toolchain baseline and freeze rule | `docs/toolchain_baseline.md` | Records AGP, Gradle, Kotlin, KSP, Room, Material and Navigation versions on 14 Nov; rule: **no version upgrades before 10 Dec** unless all three agree; Android Lint baseline run recorded | Gotam |
| FE-E.9 | ML environment | Note in `docs/toolchain_baseline.md` | Python 3.11.9 virtual environment with the Stage 2 pins; `python scripts/04_freeze.py verify` prints OK; the Stage 2 tests pass; `predict_one` runs on one sample input (needed for FE-6.1) | Abdul |
| FE-E.10 | Design tooling | Decision lines in `docs/stage3-decisions.md` | Wireframe tool chosen; one Material icon imported through Vector Asset Studio and shown in the app; no custom font | Gotam |
| FE-E.11 | Task board | GitHub Issues or Projects board | One issue per FYP-I deliverable (FE-ID in the title, owner, due date taken from this file); cut-first items labelled | Abdul |
| FE-E.12 | Commands and safety sheet | `docs/commands.md` | One page: build, unit test, instrumented test, install and lint commands; the Stage 1 safety rules (start emulators from Android Studio only; run `connectedDebugAndroidTest` only on an emulator, never on a phone holding test evidence; `assembleDebug` does not install, use `installDebug`) | Bhoomika |

---

## Phase 0 — Confirm and Spike (12–15 Nov)

**Depends on:** Phase E for FE-0.2 and FE-0.3; the other deliverables can start immediately.
**Size:** Small.
**What gets built:** the written decisions, a proof that the build still works with the new dependencies, a vault baseline check, and the supervisor's sign-off.
**Why its own phase:** a failed dependency or an unconfirmed decision found on 25 Nov costs a week you do not have.

**Acceptance criteria**
- [ ] D1 to D17 and DS-1 to DS-4 are recorded, each with an owner.
- [ ] `assembleDebug testDebugUnitTest` passes with the new dependencies.
- [ ] Stage 1 tests pass and the sweep question is answered.
- [ ] Supervisor has replied in writing.

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-0.1 | Record all decisions | `docs/stage3-decisions.md` | D1 to D17 and DS-1 to DS-4 present, each with owner and date | Team |
| FE-0.2 | Build spike: add Material 3, Navigation, fragment-ktx, coroutines-test, arch-core-testing and one empty fragment in `MainActivity`; check `security-crypto` | Updated Gradle files | `assembleDebug testDebugUnitTest` passes; `security-crypto` resolves and its deprecation status is written down with a fallback | Gotam + Bhoomika |
| FE-0.3 | Vault baseline | `docs/stage1_closeout.md` | 7 unit + 14 instrumented tests pass on the emulator; branch merged and tagged; `allowBackup=false` and Android 12+ extraction rules confirmed | Bhoomika |
| FE-0.4 | Ethics cover for the April study | Written confirmation or amendment receipt | Reply or receipt on file | Bhoomika |
| FE-0.5 | Supervisor sign-off | Meeting note or message | Covers v2 plan, Account Check layout-only, the encryption wording change | Team |
| FE-0.6 | Sweep investigation | Note in the decisions file | Every Stage 1 call site of `sweepOrphans()` listed; shared or per-instance in-flight set stated; what happens when `EvidenceListActivity` is retired stated; D13 decided | Bhoomika |

---

## Phase 1 — Design (12–17 Nov)

**Depends on:** nothing (runs alongside P2).
**Size:** Small–Medium.
**What gets built:** a frozen screen list, wireframes with all states, the corrected design basis, a contrast script, the icon mapping and the wording guide.
**Why its own phase:** the design basis has five contrast and theme problems (section 0). Cheaper to fix them on paper than in forty layouts.

**Acceptance criteria**
- [ ] Screen list frozen 16 Nov; wireframes frozen 17 Nov.
- [ ] `docs/design_basis.md` is the corrected version of your document.
- [ ] The contrast script passes for every foreground/background pair that will be used.
- [ ] Wording guide reviewed by all three.

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-1.1 | Freeze screen list and tiers | Section 3 of the v2 plan, committed | Frozen 16 Nov | Team |
| FE-1.2 | Wireframes for screens 1 to 7, including every state | Wireframe files or photos in `docs/wireframes/` | Every state in the v2 screen table appears somewhere | Gotam |
| FE-1.3 | Finalise the design basis | `docs/design_basis.md` with findings 1 to 6 applied and DS-1 to DS-4 recorded | A reader can build every colour and component without asking | Gotam |
| FE-1.3a | Contrast check script | `docs/contrast_check.py` plus its printed report | Lists every text, icon and boundary pair used by the design; each text pair is at least 4.5, each icon and boundary pair at least 3.0; reproduces your table's figures | Abdul |
| FE-1.3b | Status-to-icon mapping | Table in `docs/design_basis.md` and the chosen vector icons | Each status has a distinct icon: unchanged, changed, missing, not checked, evidence deleted, preview; licence noted | Gotam |
| FE-1.3c | Badge tint tokens | Tint values added to `docs/design_basis.md` | Each text-version colour on its tint is at least 4.5 (candidates in section 0 already pass) | Gotam |
| FE-1.4 | Wording guide | `docs/wording_guide.md` with the four fixed blocks | Reviewed by all three; the approved status labels match section 3 of the design basis | Bhoomika + Abdul |
| FE-1.5 | Quick wireframe check with 3 outside people, 10 minutes each | Short issue list | Top issues fixed or deferred. **Skip if it threatens 22 Nov.** | Abdul |

---

## Phase 2 — Vault Additions and Case Store (13–22 Nov)

**Depends on:** P0 (FE-0.3, FE-0.6).
**Size:** Medium.
**What gets built:** three additive vault methods, an encrypted case store with its repository, an in-memory verification session, and the tests that prove Stage 1 is untouched.
**Why its own phase:** the screens depend on these. They are testable without any UI, and the "existing code untouched" decision needs its own check.

**Acceptance criteria**
- [ ] Stage 1 files show added lines only in `git diff`; all Stage 1 tests pass twice.
- [ ] New data-layer tests pass.
- [ ] The case store survives a simulated mid-write failure and never resets itself on a corrupt file.
- [ ] No log line prints content, paths, hashes, notes or titles.

Use the Stage 1 test isolation pattern for every new instrumented test (separate test directory name, canonical-path check, cleanup in teardown).

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-2.1 | `verifyIntegrityDetailed(id)` with `IntegrityResult` (UNCHANGED, CHANGED, MISSING) | Added method and enum | Tests: untouched → UNCHANGED; byte flipped → CHANGED; truncated → CHANGED; file deleted → MISSING; unknown id → MISSING; old `verifyIntegrity()` untouched | Bhoomika |
| FE-2.2 | `readForDisplay(id, maxBytes)` | Added method | Image and text preview work; images downsampled while decoding; over-cap file returns "too large"; directory listing before and after shows no new files | Bhoomika |
| FE-2.3 | `importFromUri(uri, mime)` | Added method | Picked file imports with source label "In-app import"; over-limit stream gives the friendly error; failure leaves no ghost entry | Bhoomika |
| FE-2.4 | Additivity check | Note in `docs/stage1_closeout.md` | `git diff` on Stage 1 files shows no removed or changed lines; Stage 1 tests pass twice | Bhoomika |
| FE-2.5a | `CaseStore`: encrypted file read and write | `CaseStore` class | Whole file held in memory behind a mutex; raw file bytes are not readable as text; temp-file-then-replace write | Bhoomika |
| FE-2.5b | `CaseRepository` operations | `CaseRepository` class | Create, rename, delete case; add evidence item; add note item; edit note or date; remove item; list; get; evidence id that no longer exists reads as "evidence deleted" | Bhoomika + Gotam |
| FE-2.5c | Case store failure tests | Instrumented tests | Mid-write failure leaves the old file readable; a corrupt file returns an error and is **not** reset; `deleteEverything()` removes the file; removing an item or deleting a case never touches evidence | Bhoomika |
| FE-2.6 | `VerificationSession` (in-memory) | Class and unit test | Result kept across calls; empty for a new instance; nothing written to disk | Bhoomika |
| FE-2.7 | Log audit of all new code | Grep output and fixes | No log line contains content, paths, hashes, notes, titles or typed values | Bhoomika |
| FE-2.8 | *Only if D13 chooses option (c):* `sweepStaleOrphans()` | Added method and test | Removes only unreferenced files older than about 30 minutes; a file being written is untouched | Bhoomika |

---

## Phase 3 — Shell and Design System in Code (16–24 Nov)

**Depends on:** P0 (FE-0.2), P1 (FE-1.3).
**Size:** Medium.
**What gets built:** tokens in `res/values`, the Material 3 theme, `MainActivity` with navigation, the shared components, screenshot protection, a component gallery and the `strings.xml` skeleton.
**Why its own phase:** if every screen is built from these, the app looks like one product. If each screen invents its own colours, the contrast work is lost.

**Acceptance criteria**
- [ ] No hard-coded colours, sizes or user-facing strings in new layouts (script check passes).
- [ ] The gallery shows every component in every state on white, off-white and pale mint.
- [ ] The theme is Light, with no dynamic colour, on an Android 12+ emulator.
- [ ] Sharing from Gallery still saves evidence; the app opens to Home.

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-3.1a | `colors.xml` | Your starter tokens (`sh_*`) plus the tint tokens from FE-1.3c | Values match `docs/design_basis.md` exactly | Gotam |
| FE-3.1b | `themes.xml` | `Theme.ShieldHer` on a Light Material 3 parent | Every M3 role set (your mapping plus outline, surface containers, inverse, tertiary); status bar deep teal with light icons; no `DynamicColors` call; forced light mode | Gotam |
| FE-3.1c | Type, spacing and shape resources | `dimens.xml`, `styles.xml` | Five text styles (body never below 14 sp); spacing 4/8/12/16/24/32 dp; one card radius and one button radius | Gotam |
| FE-3.1d | Hard-coding check | `scripts/check_tokens.sh` | Fails on hex colours, literal `dp` values outside the tokens, and literal `android:text` in layouts; passes on the current code | Abdul |
| FE-3.2a | `MainActivity`, launcher, single-activity host | Updated manifest and activity | App opens to Home; `IntakeActivity` untouched and still the share target | Gotam |
| FE-3.2b | Navigation graph and bottom navigation (Home, Evidence, Cases, More) with placeholder fragments | Nav graph XML | Every tab opens; selected item uses deep teal | Gotam |
| FE-3.3a | `StatusBadge` | Custom view | States: unchanged, changed, missing, not checked, evidence deleted, preview; tinted background, text-version icon and text; content description equals the text | Gotam |
| FE-3.3b | `EvidenceCard` | Layout and view | White card on off-white; shows source, "Received by ShieldHer" time and a `StatusBadge` | Gotam |
| FE-3.3c | `SectionHeader`, `EmptyState`, `ErrorState` | Views | Empty state has an icon, one sentence, one action; error state has a plain explanation and a retry action | Gotam |
| FE-3.3d | `ConfirmDialog`, loading indicator, button styles | Helper and styles | Primary button deep teal with white text; destructive button #AD464E with white text; loading indicator in ocean teal; delete dialog uses wording block 3 | Gotam |
| FE-3.4 | Screenshot protection | Base behaviour and a persisted setting | `FLAG_SECURE` on screens showing evidence or hashes; default on; Recents shows a blank card; setting readable by demo mode | Gotam |
| FE-3.5 | Component gallery (debug only) | Debug screen | Every component and badge state shown on white, off-white and pale mint; absent from release builds | Gotam |
| FE-3.6 | `UiState` (Loading, Empty, Content, Error) and a ViewModel test template | Sealed class and one test | Test passes | Gotam |
| FE-3.7 | `strings.xml` skeleton | Strings file | Status labels, dialog texts, the four fixed wording blocks and the Account Check preview text, matching the wording guide | Bhoomika + Abdul |

---

## Phase 4 — Screens (20 Nov – 1 Dec)

**Depends on:** P2 and P3.
**Size:** Large.
**What gets built:** screens 1 to 7 (Must), then Onboarding, Account Check layout and Help if time allows, and a core-flow UI test.
**Why its own phase:** this is where the project becomes demonstrable to someone who is not reading code.

**Checkpoint 27 Nov:** import → list → detail → verify → case → timeline works end to end on the emulator.

**Acceptance criteria**
- [ ] The core flow works on the real vault and the case store.
- [ ] Every screen uses the shared components and has its empty, loading, error and success states.
- [ ] All text comes from `strings.xml`; the token check passes.
- [ ] No string says "verified", "authentic", "secure" or "tampered".
- [ ] Green appears only on badges and icons, never on buttons or headers.

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-4.1 | Home | Fragment and ViewModel | Counts and the three latest items from the vault; empty state when none | Gotam |
| FE-4.2a | Evidence list with search | Fragment and adapter | Lists all items; search by source and date; no-match state | Gotam |
| FE-4.2b | List row status | Adapter change | Row shows "Not checked yet" or the session result with its time; no "verify all" | Gotam |
| FE-4.3a | Evidence detail: layout and metadata | Fragment | Full hash with copy button; "Received by ShieldHer" with time zone; source; MIME; stored size labelled as encrypted size | Gotam |
| FE-4.3b | Evidence detail: in-memory preview | View code | Images and text preview through `readForDisplay`; "too large" and "unavailable" states; no file written | Gotam + Bhoomika |
| FE-4.3c | Evidence detail: Verify | Button and result area | Shows UNCHANGED, CHANGED, MISSING with the approved badge and text; result stored in `VerificationSession` | Gotam |
| FE-4.3d | Evidence detail: linked cases and "Add to case" | Section and picker | Lists the cases the item appears in; adds an item with an optional note and optional "You entered" date | Gotam |
| FE-4.3e | Evidence detail: delete | Button and dialog | Confirm dialog with wording block 3; item removed from vault; case items now read "evidence deleted" | Gotam |
| FE-4.4 | Import evidence | Flow | System picker → `importFromUri`; clear success, too-large, unsupported and failed messages | Gotam |
| FE-4.5a | Cases list | Fragment | Empty and content states | Gotam |
| FE-4.5b | Create, rename, delete case | Dialogs | Delete dialog says evidence is kept | Gotam |
| FE-4.6a | Case timeline: ordering and labels | Fragment | Items ordered by "You entered" date, falling back to "Received by ShieldHer"; each item shows which | Gotam |
| FE-4.6b | Case timeline: add evidence and add note | Pickers and forms | Evidence picker; plain note; optional "You entered" date | Gotam |
| FE-4.6c | Case timeline: remove item and "evidence deleted" display | Item actions | Removing an item keeps the evidence; a deleted item shows the grey "Evidence deleted" badge | Gotam |
| FE-4.7a | Settings and privacy: UI | Fragment | Item count; screenshot toggle; storage text (v2 plan, section 7.1); backup status; "What ShieldHer cannot prove" page; **no app-lock row** | Abdul |
| FE-4.7b | Settings: delete all | Logic | Deletes every evidence item through the repository **and** the case store, after confirmation | Bhoomika |
| FE-4.8 | Onboarding (max 3 pages) | Pages | Shown once; re-readable from Settings; text from `strings.xml` | Bhoomika + Abdul |
| FE-4.9 | Account Check layout (layout only) | Fragment | Grey "Preview: not working yet" badge; platform selector, followers, following, bio text, link yes/no; button shows only "Coming in the next version"; nothing typed is stored, logged or sent; no result colours or numbers *(Note: Layout-only scope superseded by Decision D18; on-device evaluation enabled for Instagram with frozen model)* | Abdul |
| FE-4.10 | Help and resources | Fragment | Only if Abdul verified every contact from official pages by 25 Nov; each shows "checked on <date>". Otherwise moved to FYP-II | Abdul |
| FE-4.11 | Espresso core-flow test | Instrumented test | import → detail → verify → add to case → timeline passes on the emulator, run twice | Abdul |
| FE-4.12 | State audit for screens 1 to 7 | Checklist in the repo | Every state in the v2 screen table implemented and tick-checked | Gotam |

---

## Phase 5 — Demo, Test, Freeze (30 Nov – 3 Dec)

**Depends on:** P4.
**Size:** Small–Medium.
**What gets built:** demo mode, the optional tamper tool, two-phone manual tests, privacy checks, a quick accessibility pass and the freeze.
**Why its own phase:** the jury demo needs a reliable fresh-install path, and the freeze needs a signed checklist.

**Acceptance criteria**
- [ ] Fresh install to a full demo in under a minute.
- [ ] Two-phone checklist signed.
- [ ] Merged manifest has no `INTERNET` permission.
- [ ] Accessibility Scanner shows no unresolved contrast or touch-target failures on screens 1 to 7.
- [ ] Freeze checklist (v2 plan, section 8) complete; `stage3-fyp1` tagged and merged on 3 Dec.

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-5.1a | Demo seeder: evidence | Debug-only code | 6 to 8 synthetic items inserted through `intake()` with source label "Demo data" | Gotam |
| FE-5.1b | Demo seeder: cases | Debug-only code | Two cases with timelines flagged `isDemo` | Gotam |
| FE-5.1c | Demo banner and reset | Debug-only code | "Demo data" banner; screenshot protection off while on; Reset removes only demo items and demo cases, tested with real items present | Gotam |
| FE-5.2 | Tamper tool (D16, **first to cut**) | Debug-only menu and an added `BuildConfig.DEBUG` hook | Live demo shows "Changed or unreadable"; hook and menu absent from a release build (checked) | Abdul + Bhoomika |
| FE-5.3a | Manual checklist, phone 1 | Signed checklist | Import, share, verify, tamper, delete, case flow, airplane mode, force-stop during import, blank Recents card | Team |
| FE-5.3b | Manual checklist, phone 2 (different manufacturer if possible) | Signed checklist | Same items | Team |
| FE-5.4a | Merged-manifest check | Note | No `INTERNET` permission | Bhoomika |
| FE-5.4b | Privacy review of new code | Findings | No new file outside the file store and case store; no logs of content, notes, titles or hashes | Bhoomika |
| FE-5.6 | Quick accessibility pass | Scanner report and fixes | Accessibility Scanner on screens 1 to 7; contrast results match `docs/contrast_check.py`; touch targets at least 48 dp. (The 200% font-scale check stays in FE-7.5.) | Abdul |
| FE-5.5 | Freeze | Tag `stage3-fyp1` and reviewed PR | Checklist complete; merged on 3 Dec | Team |

---

## Phase 6 — Fixes, Slides, Rehearsal (4–10 Dec)

**Depends on:** P5.
**Size:** Small–Medium.
**What gets built:** no new features. Bug fixes, wording fixes, the jury slides with real offline risk results, the deviations file, a demo script and rehearsals.

**Acceptance criteria**
- [ ] Only fixes, wording changes and tests land after 3 Dec.
- [ ] Slides label every risk result "offline output of the frozen v1 model, not shown in the app yet".
- [ ] Two timed rehearsals done on the demo device.

| ID | Deliverable | Output | Acceptance check | Owner |
|---|---|---|---|---|
| FE-6.1 | Jury slides with real account-risk results | Slides | Run offline with frozen v1 `predict_one` on a few fixed inputs; limits text beside every result; Cresci per-group table included; no bare probability; nothing called "fake" or "bot" | Abdul |
| FE-6.2 | Master deviations file | `docs/proposal_deviations.md` | Merges the `ml/` file with vault and front-end items, including the encryption sentence change; supervisor has read it | Bhoomika |
| FE-6.3 | Slides, architecture diagram and Gantt updated | Updated files | Dashboard shown as the third component; deferred items marked; figures render on the presenting device | Team |
| FE-6.4 | Demo script and fallback video | Script and video | 5 minutes, run twice on a debug install holding no real evidence | Gotam |
| FE-6.5 | Q&A for the new scope | Q&A file | One named answerer per question; mock Q&A in random order | Team |
| FE-6.6 | Two timed rehearsals | Notes | Inside the time limit | Team |

---

## Phases 7 to 10 — FYP-II (outline; detail comes after the December submission)

### Phase 7 — FYP-II build (4 Jan – 26 Feb 2027)

| ID | Deliverable | Acceptance check | Owner |
|---|---|---|---|
| FE-7.1 | Connect Account Check: FastAPI service wrapping `predict_one`, pinned Docker image (Python 3.11.9, scikit-learn 1.9.1, pandas 3.0.6, numpy 2.4.6, shap 0.51.0), no request logging, HTTPS in release *(Note: Replaced in FYP-I by on-device evaluation per Decision D18; server backend not required)* | Same outputs as `predict_one` on 10 fixed inputs; no feature values in logs | Abdul |
| FE-7.1a | Account Check result states, in the design colours | Supported result, **uncertain (amber badge, text-version icon)**, **insufficient context or unsupported input (grey)**, "not validated for this platform" (grey), service unavailable; limits block always visible; each uses icon + colour + text | Abdul + Gotam |
| FE-7.2 | Save a check to a case as a labelled item | Model version, training domain and tested conditions stored with it | Abdul |
| FE-7.3 | Help (if postponed), Case summary (read-only, from real case data, no export), SafeShare Preview (labelled mock or read-only EXIF check) | Each labelled honestly | Bhoomika / Gotam |
| FE-7.4 | App lock (BiometricPrompt with device credential), if wanted | Works on two devices, or stays hidden | Bhoomika |
| FE-7.5 | State completeness, small phones, **200% font scale without clipped text**, full accessibility pass (TalkBack, Accessibility Scanner, 48 dp targets, contrast) | Fix list closed or consciously deferred | Gotam + Abdul |
| FE-7.6 | `demo` build variant: release-like, with demo seeder and banner, no tamper tool | Usable for the April study | Gotam |
| FE-7.7 | Checkpoint 26 Feb | Done list and cut decisions written down | Team |

TreeSHAP stability (RQ1) is Abdul's separate FYP-II work and is not part of this front-end plan.

### Phase 8 — Hardening (1–26 Mar 2027)

FE-8.1 full regression and two-phone checklist · FE-8.2 privacy audit round 2 (logs, screenshot protection coverage, nothing decrypted on disk, backup, network only from Account Check, cleartext blocked in release) · FE-8.3 security review and threat-model update, stating the plaintext vault DB as a disclosed limit · FE-8.4 performance (500 seeded items, large-file Verify) · FE-8.5 release build contains no tamper tool, gallery or demo mode · FE-8.6 usability pilot with two people.

### Phase 9 — Usability study (1–23 Apr 2027)

FE-9.1 protocol, consent, tasks, SUS and comprehension questions, with thresholds written **before session 1** · FE-9.2 8 to 12 proxy-user sessions on the `demo` variant with synthetic data only · FE-9.3 analysis · FE-9.4 fix the top issues. Comprehension questions include: "Does *Unchanged since saved* mean the screenshot is genuine?" and "What does *Not validated for this platform* tell you?"

### Phase 10 — Final (26 Apr – May 2027)

FE-10.1 documentation (design rationale, screen guide, wording guide, usability results, deviations) · FE-10.2 how-to-run notes · FE-10.3 individual contribution sections · FE-10.4 final defense rehearsal.

---

## Counts and critical path

- **FYP-I (PE to P6):** about 87 deliverables, of which 8 are conditional or cuttable (FE-2.8, FE-4.8, FE-4.9, FE-4.10, FE-5.2 and the optional checks FE-1.5 and FE-4.12 if time is tight).
- **Critical path:** FE-E.2 → FE-E.7 → FE-E.8 → FE-0.2 → FE-3.1a/b → FE-3.3a → FE-4.3 → FE-4.11 → FE-5.5. In parallel: FE-2.1 → FE-2.5 must be done before FE-4.3c and FE-4.6.
- **Conflict to watch:** Gotam owns most of P3 and P4. If he is behind at the 22 Nov checkpoint, move FE-3.3d, FE-3.5 and FE-4.5b to Abdul first.
