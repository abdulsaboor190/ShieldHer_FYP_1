# ShieldHer Stage 3: Phase 0 and Phase 1 Prompts

**Written:** 16 November 2026
**Repository:** https://github.com/abdulsaboor190/ShieldHer_FYP_1.git
**Working folder:** `D:\23i-3039\P2\ShieldHer1\Shieldher` (git root, Android project). The sibling `ml` folder is Stage 2, frozen, in its own repo.
**Phase 0 (Confirm and Spike)** was due Sun 15 Nov. **Phase 1 (Design)** is due Tue 17 Nov. You are about a day behind, so run the prompts in parallel where the table below allows.

**Checkpoints that follow:** Sun 22 Nov (vault additions done, shell navigates), Fri 27 Nov (core flow works end to end), **freeze Thu 3 Dec**, submission Thu 10 Dec.

---

## Order and who runs what

| Order | ID | What | Type | Who | Branch |
|---|---|---|---|---|---|
| A | FE-0.1 + FE-1.1 | Record decisions, freeze screen list | Agent | Abdul | `fe/p0-decisions` |
| A | FE-0.6 | `sweepOrphans()` investigation (read-only) | Agent | Bhoomika | `fe/p0-sweep` |
| A | FE-0.2 | Build spike (new dependencies) | Agent | Gotam | `fe/p0-build-spike` |
| A | FE-0.3 | Vault baseline on an emulator | Agent + Human | Bhoomika | `fe/p0-vault-baseline` |
| A | FE-0.5 | Supervisor sign-off message | Human | Abdul | none |
| A | FE-0.4 | Ethics cover for the April study | Human | Bhoomika | none |
| B | FE-1.3a | Contrast check script | Agent | Abdul | `fe/p1-contrast` |
| B | FE-1.3 + 1.3b + 1.3c | Corrected design basis | Agent | Gotam | `fe/p1-design-basis` |
| B | FE-1.4 | Wording guide | Agent | Abdul | `fe/p1-wording-guide` |
| C | FE-1.2 support | Screen specs for the wireframes | Agent | Gotam | `fe/p1-screen-specs` |
| C | FE-1.2 | Wireframes in Figma (screens 1 to 7) | Human | Gotam | none |
| C | FE-1.5 | Wireframe check with 3 outside people (optional) | Human | Abdul | none |

**Group A** can all start today, in parallel. **Group B** can start today too. **Group C** waits for the design basis and wording guide to be merged.

**Only FE-0.2 may change Gradle files or `app/src`.** Every other prompt writes only under `docs/`.

---

## How to run each prompt

```powershell
cd D:\23i-3039\P2\ShieldHer1\Shieldher
git switch main
git pull
git status --short          # must print nothing
```

1. Paste the **Standing rules** (below), then the prompt.
2. Read the agent's report and run the **"You check"** list.
3. The agent commits and pushes **its own task branch**. **You** open the pull request, get a teammate to review it, and merge it on GitHub. Then `git switch main`, `git pull`, and confirm `git status --short` is empty before the next prompt.

---

## Standing rules (paste at the top of every prompt)

```text
STANDING RULES (apply to the whole task)
1. Work ONLY inside the git root D:\23i-3039\P2\ShieldHer1\Shieldher. Do not open, edit or create anything in the sibling `ml` folder or the outer folder. NEVER search outside this repo: if a file is missing, STOP and say so.
2. Files under app/src/main/java/com/shieldher/vault are Stage 1 code. Do NOT touch them in this task.
3. START CONDITION: run `git status --short` and `git branch --show-current`. The tree must be clean and the branch must be `main`. Run `git pull`. If not, STOP and tell me. NEVER create a branch from any branch other than an up-to-date main.
4. Do not modify any existing file unless this prompt names it. Where a prompt says a file is a "starter", keep its title line and any section the prompt says to keep. Never overwrite any other existing file.
5. Do not edit any Gradle file, the manifest, or anything under app/src unless this prompt explicitly says so.
6. Never start an emulator. Never run connectedDebugAndroidTest unless this prompt says so and gives an emulator serial. Never run installDebug without ANDROID_SERIAL set.
7. GIT PERMISSIONS: you MAY create the branch named in the prompt, commit on it (one commit per deliverable, message given in the prompt), and run `git push -u origin <that branch>` at the end. You must NOT merge, rebase, force-push, tag, change git config, or push to main.
8. Never print secrets (keystores, passwords, tokens, the contents of local.properties).
9. Use PowerShell syntax on Windows (for example `.\gradlew.bat`, `Select-String`, `Get-ChildItem`).
10. If anything conflicts with these rules or surprises you, STOP and tell me before continuing.

REPORT BACK (every task)
- Files created or changed (full paths) and files skipped
- Commands run and their key output
- Output of `git status --short`, `git diff main --stat` and `git log --oneline -5`
- Build and test results (when the prompt runs them)
- Anything surprising
```

---

## GROUP A: Phase 0

### Prompt A1. FE-0.1 and FE-1.1: Record decisions and freeze the screen list

**Branch:** `fe/p0-decisions`
**Commits:** `FE-0.1 FE-1.1: record decisions and freeze screen list`
**Before you run:** this assumes you accept my recommendations for D12 to D17 and DS-1 to DS-4. If you disagree with any row, edit it in the prompt first.

```text
TASK: FE-0.1 and FE-1.1. Record the Stage 3 decisions and freeze the screen list in docs/stage3-decisions.md. Documentation only.

BRANCH: after the start condition passes, run `git switch -c fe/p0-decisions`.

FILE RULES: docs/stage3-decisions.md already exists (title line plus a "Design tooling" section). KEEP the title and the "Design tooling" section exactly as they are and APPEND the two new sections below.

SECTION 1: append "## Decisions (recorded 16 Nov 2026)" with a table with columns: ID | Decision | Owner | Status. Rows:
D1 | Views + Material 3, not Compose | Gotam | Locked
D2 | English only; all text in strings.xml | Team | Locked
D3 | Account Check is layout only in FYP-I; no result shown; real results appear only in the jury slides | Team | Locked
D4 | Feature freeze Thu 3 Dec 2026; submission Thu 10 Dec 2026 | Team | Locked
D5 | Vault changes: three additive methods only (verifyIntegrityDetailed, readForDisplay, importFromUri); existing vault code untouched; no database migration | Bhoomika | Locked
D6 | Cases live in a separate small store; vault untouched | Bhoomika + Gotam | Locked
D7 | No SQLCipher; the proposal sentence is changed to say evidence files are encrypted and the vault metadata database is not | Bhoomika | Locked
D8 | Screenshot protection (FLAG_SECURE) on by default with a Settings toggle; demo mode turns it off with a banner | Team | Locked
D9 | Two kinds of time: "Received by ShieldHer" (phone clock, never editable) and "You entered" (optional incident date), always labelled | Team | Locked
D10 | Integrity is checked only when the user taps Verify; results are kept in memory for the session | Bhoomika | Locked
D11 | Demo mode: fake evidence and two fake cases, bannered "Demo data", with reset | Gotam | Locked
D12 | Case store is an encrypted file (Jetpack Security EncryptedFile), written atomically | Bhoomika + Gotam | Accepted (16 Nov)
D13 | New screens never call sweepOrphans() (option b), pending the FE-0.6 findings | Bhoomika | Accepted pending FE-0.6
D14 | Notes and incident dates live on case items, not on evidence rows | Team | Accepted (16 Nov)
D15 | No app lock in FYP-I; no lock row in Settings | Team | Accepted (16 Nov)
D16 | Tamper-demo tool: yes, debug builds only, first to cut | Abdul + Bhoomika | Accepted (16 Nov)
D17 | Help screen moves to FYP-II unless Abdul has verified every contact from official pages by 25 Nov | Abdul | Accepted (16 Nov)
DS-1 | "Changed or unreadable since saved" uses muted red with a warning icon, a different icon from "File missing"; amber is reserved for "uncertain" (Account Check, FYP-II) | Team | Accepted (16 Nov)
DS-2 | Badges use a light tinted background with a text-version colour for text and icon | Gotam | Accepted (16 Nov)
DS-3 | Light theme only, forced; never call DynamicColors | Gotam | Accepted (16 Nov)
DS-4 | Material vector icons, Apache 2.0 (already recorded under Design tooling) | Gotam | Locked

SECTION 2: append "## Screen list (frozen 16 Nov 2026)":
1 Home (Must); 2 Evidence list (Must); 3 Evidence detail (Must); 4 Import evidence (Must); 5 Cases list and create (Must); 6 Case timeline (Must); 7 Settings and privacy (Must); 8 Demo mode, debug builds only (Must for the jury demo); 9 Onboarding, max 3 pages (Should); 10 Account Check, layout only (Should); 11 Help and resources (moved to FYP-II unless verified by 25 Nov, see D17).
Then add: "Cut order if time runs out: 11, 10, 9, tamper tool, polish of 7. Never cut: screens 2 to 6, the Verify button, demo mode, Settings delete-all and the limits page."

FINISH: `git status --short` should show only docs/stage3-decisions.md modified; `git diff main --stat` must show added lines only. Commit with the message above and run `git push -u origin fe/p0-decisions`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules.
```

**You check:** only `docs/stage3-decisions.md` changed, with added lines only, and the existing "Design tooling" section is intact.

---

### Prompt A2. FE-0.6: Investigate `sweepOrphans()`

**Branch:** `fe/p0-sweep`
**Commit:** `FE-0.6: sweepOrphans investigation findings`
**Why:** this decides D13 and whether Phase 2 is safe. Nothing in `app/src` may change.

```text
TASK: FE-0.6 Investigate how sweepOrphans() is used in Stage 1 and whether the new screens could trigger a data-loss race. READ-ONLY on app/src; the only file written is docs/stage3-decisions.md.

BRANCH: after the start condition passes, run `git switch -c fe/p0-sweep`.

STEP 1: INVESTIGATE (read-only)
a) Use Select-String across app/src (main, test, androidTest) to find every reference to `sweepOrphans`. For each: file, line number, enclosing class and function.
b) Open the class that defines sweepOrphans and any "in-flight" tracking it uses. Report exactly how the in-flight set is declared: an instance field, a companion object or top-level object (shared by all instances), or something else; and whether it is thread-safe.
c) Find every place EvidenceRepository is constructed (for example IntakeActivity, EvidenceListActivity, tests). Report whether each construction creates its own instance.
d) Find which Stage 1 screen triggers the sweep today.

STEP 2: ANSWER IN PLAIN LANGUAGE (label each statement VERIFIED IN CODE or INFERENCE)
1) Can a sweep started from one repository instance delete a file that another instance is still writing during an import?
2) What happens to the sweep when EvidenceListActivity is replaced by a fragment in a new MainActivity? Would the only call site disappear?
3) What would an additive sweepStaleOrphans() (removes only unreferenced files older than about 30 minutes) need to look like, in two or three sentences?

STEP 3: RECORD
Append to docs/stage3-decisions.md (keep everything already there) a section "## FE-0.6 findings (16 Nov 2026)" containing the call-site table, the in-flight declaration, the answers above, and a recommendation for D13: keep option (b) (new screens never call the sweep), or choose option (c) (additive sweepStaleOrphans()), or document the drop. Do NOT edit any code.

FINISH: `git status --short` must show only docs/stage3-decisions.md modified; `git diff main --stat` must show added lines only. Commit with the message "FE-0.6: sweepOrphans investigation findings" and run `git push -u origin fe/p0-sweep`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the call-site table and the two answers in full.
```

**You check:** nothing under `app/src` changed. Read the findings carefully, because they decide whether Phase 2 needs FE-2.8.

---

### Prompt A3. FE-0.2: Build spike (new dependencies)

**Branch:** `fe/p0-build-spike`
**Commit:** `FE-0.2: build spike with navigation, fragment, lifecycle and test libraries`
**Why:** proves the new libraries compile with AGP 9.4.1 and your Kotlin version before anyone builds on them. **This is the only prompt allowed to edit Gradle files and `app/src`.**
**Note:** the toolchain freeze (no upgrades before 10 Dec) still applies. This prompt only **adds** new libraries and never upgrades an existing one.

```text
TASK: FE-0.2 Build spike. Add the new libraries the dashboard needs, prove they compile, and check the security-crypto status. Do NOT upgrade any existing version.

EXCEPTION TO THE STANDING RULES (this task only): you MAY edit gradle/libs.versions.toml, app/build.gradle.kts, and create files under app/src/debug and app/src/test and the docs files named below. You may NOT edit the manifest, anything under app/src/main, or any existing Stage 1 file.

BRANCH: after the start condition passes, run `git switch -c fe/p0-build-spike`.

STEP 1: INSPECT (read-only)
Report which of these libraries are already declared (in libs.versions.toml or app/build.gradle.kts) and at what versions: androidx.fragment (fragment-ktx), androidx.lifecycle (viewmodel-ktx, runtime-ktx), androidx.navigation (navigation-fragment-ktx, navigation-ui-ktx), kotlinx-coroutines (android and test), androidx.arch.core core-testing, androidx.activity, appcompat, constraintlayout, recyclerview. Also report the exact versions of AGP, Kotlin, KSP, compileSdk and Material.

STEP 2: ADD DEPENDENCIES (one at a time)
Add through gradle/libs.versions.toml, in the same style that file already uses. Add only those not already present:
  implementation: androidx.fragment:fragment-ktx; androidx.lifecycle:lifecycle-viewmodel-ktx and lifecycle-runtime-ktx; androidx.navigation:navigation-fragment-ktx and navigation-ui-ktx; androidx.recyclerview:recyclerview
  testImplementation: org.jetbrains.kotlinx:kotlinx-coroutines-test; androidx.arch.core:core-testing
Rules for choosing versions: choose the latest STABLE release (no alpha, beta or rc) for each library. If network access to the Google and Maven Central metadata is not available, STOP and tell me what you need. Do NOT use Safe Args or any new Gradle plugin. After EACH library, run `.\gradlew.bat assembleDebug`. If it fails with a Kotlin metadata incompatibility, a minCompileSdk or AGP requirement, step down to the previous stable version of that library; try at most three versions per library, then STOP and report. Never change AGP, Gradle, Kotlin, KSP, Room, Material, compileSdk or any existing version.

STEP 3: SPIKE FILES (debug and test only)
a) app/src/debug/java/com/shieldher/dashboard/debug/SpikeFragment.kt: a minimal Fragment that builds a plain TextView in code (no layout file, no strings.xml entry; this is a debug-only spike).
b) app/src/debug/res/navigation/spike_nav_graph.xml: a navigation graph with one destination pointing at SpikeFragment.
c) app/src/test/java/com/shieldher/dashboard/SpikeToolchainTest.kt: one unit test that uses kotlinx-coroutines-test (runTest) and InstantTaskExecutorRule with a trivial class defined inside the test file, proving both test libraries work.
These files live in the debug source set and the test folder so they never ship in a release build. Do not touch the manifest or MainActivity.

STEP 4: VERIFY
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass. Report the number of unit tests passed (Stage 1 had 7; expect 8).
b) Run `.\gradlew.bat :app:compileReleaseKotlin` and confirm SpikeFragment does NOT appear in the release compiled classes (search app\build for SpikeFragment under a release path and report). If compileReleaseKotlin fails for a reason that also fails on main, report it and do not fix it.
c) security-crypto: run `.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath` and report the resolved version of androidx.security:security-crypto. Report whether ANY build output mentioned it as deprecated. If you saw no deprecation message, write "no deprecation flag seen in build output; status unknown, check Maven or the AndroidX release notes" (do not claim it is or is not deprecated).
d) Report any NEW build warnings compared with the FE-E.8 baseline.

STEP 5: RECORD
a) Update docs/tech_register.md: fill in the Version column for the rows that were "[fill in - FE-0.2]" (fragment-ktx, lifecycle, Navigation, coroutines-test, arch-core-testing) and update the security-crypto row with the real resolved version and status text from Step 4c. Change nothing else in that file.
b) Append to docs/toolchain_baseline.md a section "## Dependencies added in FE-0.2 (16 Nov 2026)" listing each library, its version, and whether any step-down was needed. Keep the freeze rule: the baseline now includes these versions.

FINISH: `git status --short` must show only the files named above. `git diff main --stat` must show no removed lines in app/build.gradle.kts and gradle/libs.versions.toml except none (additions only). Commit with the message above and run `git push -u origin fe/p0-build-spike`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the Step 1 inventory, the version table of everything added, the unit-test count, the release-exclusion result, and the security-crypto finding.
```

**You check:**
- `git diff main --stat` shows only the two Gradle files (added lines), the three spike files, and two docs.
- 8 unit tests pass, and `SpikeFragment` is absent from the release build.
- No existing version changed.

---

### Prompt A4. FE-0.3: Vault baseline on an emulator [AGENT + HUMAN]

**Branch:** `fe/p0-vault-baseline`
**Commit:** `FE-0.3: Stage 1 vault baseline results`
**Human steps first:**
1. In Android Studio, start **one emulator** (not a phone) and wait for the home screen.
2. Run `adb devices -l` and note the serial (it must start with `emulator-`).
3. Paste the prompt with `<EMULATOR_SERIAL>` filled in.

```text
TASK: FE-0.3 Establish the Stage 1 vault baseline: run the Stage 1 tests, check the backup settings, and audit the logging in the vault code. Emulator serial: <EMULATOR_SERIAL>. Write only docs/stage1_closeout.md.

BRANCH: after the start condition passes, run `git switch -c fe/p0-vault-baseline`.

STEP 1: EMULATOR SAFETY CHECK
The serial must start with "emulator-". If it does not, STOP: instrumented tests must never run on a phone. Run `adb devices -l` and confirm the serial is listed and booted.

STEP 2: RUN THE TESTS
a) `.\gradlew.bat testDebugUnitTest`: report the number passed (Stage 1: 7).
b) Set `$env:ANDROID_SERIAL="<EMULATOR_SERIAL>"`, then run `.\gradlew.bat connectedDebugAndroidTest`: report passed, failed and skipped (Stage 1 expects 14). If any test fails, copy the failing test names and the first lines of each error. Do NOT fix anything.

STEP 3: GIT STATE (read-only)
Run `git tag -l` and report whether stage1-baseline exists and which commit it points to (`git rev-list -n 1 stage1-baseline`). Report whether the stage1-baseline commit is an ancestor of main (`git merge-base --is-ancestor stage1-baseline main`).

STEP 4: BACKUP SETTINGS (read-only)
Open app/src/main/AndroidManifest.xml and report the exact values of android:allowBackup, android:fullBackupContent and android:dataExtractionRules. If the manifest references XML files (for example under app/src/main/res/xml), print their contents. State in plain language whether the app's database and files are excluded from (a) cloud backup and (b) device-to-device transfer on Android 12 and later. Label each statement VERIFIED IN FILES or INFERENCE. Do NOT edit the manifest.

STEP 5: LOG AUDIT (read-only)
Search app/src/main/java/com/shieldher/vault for Log., println, Timber and printStackTrace. List every match as file:line with the line text. For each, mark whether it could print content, a file path, a hash, or a user-entered value (VERIFIED if the logged value is visible in the line; otherwise INFERENCE). Do NOT edit any code.

STEP 6: RECORD
Append to docs/stage1_closeout.md (keep everything already in it, including the "Known Stage 1 lint errors" section) a section "## FE-0.3 results (16 Nov 2026)" with: unit and instrumented test counts, the tag and ancestry result, the backup findings, the log audit table, and a short "Open items" list (anything failed, anything uncertain).

FINISH: `git status --short` must show only docs/stage1_closeout.md modified; `git diff main --stat` must show added lines only. Commit with the message "FE-0.3: Stage 1 vault baseline results" and run `git push -u origin fe/p0-vault-baseline`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the test counts, the backup findings and the log audit table.
```

**You check:**
- 14 instrumented tests ran on the emulator.
- Backup: if `allowBackup` is not `false` or the extraction rules don't exclude app data, tell me. The proposal sentence must not claim backup is disabled until this is confirmed.
- Any log line that prints a path, hash or user text is a privacy problem to raise with Bhoomika.

---

### FE-0.5: Supervisor sign-off (human, send today)

Send this by email or message. Replies take days, so don't wait for FE-0.2 or FE-0.3.

```text
Subject: ShieldHer FYP-I: revised scope for sign-off

Dear Sir/Madam [supervisor name],

Following the jury's advice, our third component is now a dashboard / front-end for ShieldHer. We would like your approval of the following revised FYP-I scope:

1. The dashboard is the third component, built on the existing evidence vault. It covers Home, Evidence list and detail, Import, Cases (a case timeline), Settings and privacy, and a demo mode.
2. The Account Check screen is a layout only in FYP-I. It does not show any model result in the app; the account-risk results will be presented in our slides, and the screen will be connected in FYP-II.
3. Face-forgery detection and the text classifier are not part of FYP-I and are deferred or dropped. We will record this in our deviations document.
4. The evidence files are encrypted. The vault's technical metadata database is not, so we will correct the proposal sentence that said it was, and state this as a disclosed limitation.
5. Our feature freeze is Thursday 3 December and submission is Thursday 10 December.

Could you also confirm: (a) the date of our FYP-I defense after submission, and (b) whether our ethics determination covers a usability study with 8 to 12 classmates or volunteers (no real victims, synthetic data only) in April 2027, or whether we should submit an amendment.

Thank you.
[Names and roll numbers]
```

### FE-0.4: Ethics cover (human, Bhoomika)

Ask the supervisor or the ethics office whether the existing determination covers an April 2027 usability study with 8 to 12 proxy users, informed consent, anonymous participant IDs, synthetic demo data only, and no real victims. If not, submit an amendment. Save the written reply in `docs/stage3/` (for example `ethics_confirmation.md` or a PDF note), and record it in `docs/stage1_closeout.md`.

---

## GROUP B: Phase 1

### Prompt B1. FE-1.3a: Contrast check script

**Branch:** `fe/p1-contrast`
**Commit:** `FE-1.3a: contrast check script`

```text
TASK: FE-1.3a Create the contrast check script and its report. Standard-library Python only. Create exactly two files: docs/contrast_check.py and docs/contrast_report.txt.

BRANCH: after the start condition passes, run `git switch -c fe/p1-contrast`.

STEP 1: WRITE docs/contrast_check.py
Use ONLY the Python 3 standard library. It implements the WCAG 2 relative-luminance contrast ratio. It checks the pair list below and prints one line per pair: name, foreground, background, ratio to 2 decimals, minimum, PASS or FAIL. It exits with a non-zero code if any REQUIRED pair fails. For the DECORATION-ONLY list it prints "EXPECTED FAIL (do not use for text or icons)" when the pair fails, and prints a warning if one unexpectedly passes.

REQUIRED text pairs (minimum 4.5):
- main #263635 on #FFFFFF, #F8FAF7, #E4F0E9, #A8D5C8
- secondary text #5A6662 on #FFFFFF, #F8FAF7, #E4F0E9
- white #FFFFFF on #164E50 and on #267878
- deep teal #164E50 on #FFFFFF, #F8FAF7, #E4F0E9, #A8D5C8
- ocean teal #267878 on #FFFFFF and #F8FAF7
- text-version colours green #2F6F5A, amber #8F5E12, grey #666A72, red #AD464E, each on #FFFFFF, #F8FAF7 and #E4F0E9
- badge tints: #2F6F5A on #E6F2EE, #8F5E12 on #FBF1E0, #666A72 on #ECEDEF, #AD464E on #F8E8E9
- white #FFFFFF on #AD464E (destructive button)
- main #263635 on #D69A42
REQUIRED non-text pairs (minimum 3.0): ocean teal #267878 and deep teal #164E50 each on #FFFFFF, #F8FAF7, #E4F0E9; outline #75817D on #FFFFFF, #F8FAF7, #E4F0E9.
DECORATION-ONLY expected failures: #55A6A0 on #FFFFFF, #F8FAF7, #E4F0E9 (minimum 3.0); #D69A42 on #FFFFFF (3.0); #898D95 on #FFFFFF (3.0); the old muted text #75817D on #FFFFFF (4.5).
Add a header comment explaining that the script must be re-run after any colour change.

STEP 2: RUN
Run `py -3 docs/contrast_check.py` (or `python docs/contrast_check.py`), and save the printed output, plain ASCII, to docs/contrast_report.txt. Report the exit code and any REQUIRED pair that failed. Do not change any colour to make it pass; report instead.

FINISH: `git status --short` must show only the two new files. Commit with the message above and run `git push -u origin fe/p1-contrast`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the full script output and the exit code.
```

**You check:** exit code 0, and every REQUIRED pair passes. Every decoration-only pair shows `EXPECTED FAIL`.

---

### Prompt B2. FE-1.3, 1.3b and 1.3c: Corrected design basis

**Branch:** `fe/p1-design-basis`
**Commit:** `FE-1.3: corrected design basis`

```text
TASK: FE-1.3, FE-1.3b and FE-1.3c. Rewrite docs/design_basis.md with the corrected colour system, status mapping and badge tints. Documentation only. Do NOT create any drawable or resource file.

BRANCH: after the start condition passes, run `git switch -c fe/p1-design-basis`.

FILE RULES: docs/design_basis.md exists with a title line and an "Icons" section at the end. KEEP the title line and KEEP the existing "Icons" section exactly as it is (move it to the end if needed). Replace everything between with the sections below. If docs/reference/design_v1.md exists, read it for the original wording; if it does not exist, continue from the values below.

SECTIONS
1. Direction: modern, calm, trustworthy, warm, not clinical. Do not make the whole UI green: balance white surfaces, pale mint areas and deep teal. No neon, no heavy gradients. Never rely on colour alone: every status uses icon plus text plus colour.
2. Core palette (table: Role | Name | Hex | Use): Primary deep teal #164E50 (headers, primary buttons, selected navigation); Secondary ocean teal #267878 (highlights, links, loading indicators, selected states); Accent soft turquoise #55A6A0 (DECORATION ONLY: never text, never icons, never loading or selected states, because it fails 3:1 on white 2.86, off-white 2.72 and pale mint 2.44); Supporting warm mint #A8D5C8 (sparingly; only main text #263635 or deep teal #164E50 on it); Light surface pale mint #E4F0E9; Background off-white #F8FAF7; Card white #FFFFFF; Main text #263635; Secondary text #5A6662 (darkened from #75817D, which was 4.0 on white).
3. Semantic colours (table: Meaning | Fill, tinted background only | Text and icon colour): Success green fill #38836B, text/icon #2F6F5A; Uncertain amber fill #D69A42, text/icon #8F5E12; Neutral or insufficient context grey fill #898D95, text/icon #666A72; Error or destructive red fill #C45B62, text/icon #AD464E. Rule: fill colours are never used for icons or text and never as a solid background behind white text; icons and text use the text/icon colour.
4. Badge tint tokens (FE-1.3c): success tint #E6F2EE with text #2F6F5A; amber tint #FBF1E0 with #8F5E12; grey tint #ECEDEF with #666A72; red tint #F8E8E9 with #AD464E. Destructive button: solid #AD464E with white text.
5. State-to-colour and icon mapping (FE-1.3b), planned icon file names only (DO NOT create drawables now): Unchanged since saved: green, ic_status_unchanged (exists); Changed or unreadable since saved: red, ic_status_changed (warning icon, distinct from missing); File missing: red, ic_status_missing (distinct icon); Not checked yet: grey, ic_status_not_checked; Evidence deleted: grey, ic_status_deleted; Preview: not working yet (Account Check): grey, ic_status_preview. Green is used only on badges and icons, never on buttons or headers. Account Check shows no result colours.
6. Material 3 role mapping (fixed Light theme, dynamic colour off): primary #164E50; onPrimary #FFFFFF; primaryContainer #E4F0E9; onPrimaryContainer #164E50; secondary #267878; onSecondary #FFFFFF; secondaryContainer #A8D5C8; onSecondaryContainer #164E50; tertiary #55A6A0; onTertiary #263635; tertiaryContainer #A8D5C8; onTertiaryContainer #164E50; background #F8FAF7; onBackground #263635; surface #FFFFFF; onSurface #263635; surfaceVariant #E4F0E9; onSurfaceVariant #5A6662; outline #75817D (boundaries only, 3:1); outlineVariant #A8D5C8; surfaceContainerLowest #FFFFFF; surfaceContainerLow #F8FAF7; surfaceContainer #F8FAF7; surfaceContainerHigh #E4F0E9; surfaceContainerHighest #E4F0E9; inverseSurface #263635; inverseOnSurface #F8FAF7; inversePrimary #A8D5C8; error #AD464E; onError #FFFFFF; errorContainer #F8E8E9; onErrorContainer #AD464E; scrim #000000. Rules: never call DynamicColors; force light mode (DS-3); the component gallery (FE-3.5) must show every component on white, off-white and pale mint to catch default-colour leaks.
7. Layout tokens (proposed defaults, confirmed at FE-3.1c): spacing 4/8/12/16/24/32 dp; five text styles with body never below 14 sp; one card radius and one button radius; touch targets at least 48 dp; system font only; no hard-coded colours, sizes or user-facing strings in layouts; EmptyState uses a Material vector icon (no illustrations).
8. Components: StatusBadge (tinted background, text-version icon and text, content description equal to the text); EvidenceCard (white on off-white); SectionHeader; EmptyState; ErrorState; ConfirmDialog (delete uses wording block 3 in docs/wording_guide.md); loading indicator in ocean teal; primary button deep teal with white text; destructive button #AD464E with white text.
9. Contrast rules: text at least 4.5:1; icons and UI boundaries at least 3:1; verified by docs/contrast_check.py (re-run after any colour change; the script may not exist on main yet if its PR is unmerged, in which case say "see docs/contrast_check.py once merged") and by Accessibility Scanner on real screens.
10. "Superseded": docs/reference/design_v1.md is superseded where it says amber or grey fills may be used for icons, or soft turquoise may be used for loading or selected states.
Then the existing "Icons" section.

FINISH: `git status --short` must show only docs/design_basis.md modified; `git diff main --stat` must show changes in that file only, and the "Icons" section must be unchanged. Commit with the message above and run `git push -u origin fe/p1-design-basis`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules.
```

**You check:** the Icons section is unchanged, and no drawable was created.

---

### Prompt B3. FE-1.4: Wording guide

**Branch:** `fe/p1-wording-guide`
**Commit:** `FE-1.4: wording guide`

```text
TASK: FE-1.4 Write the wording guide. Documentation only.

BRANCH: after the start condition passes, run `git switch -c fe/p1-wording-guide`.

FILE RULES: docs/wording_guide.md exists as a starter. KEEP its title line and replace the body with:

1. Tone: calm, plain, supportive, non-judgemental; short sentences; users may be in distress.
2. A table Use | Avoid:
   "Saved to this phone" | "Secured", "Protected" (unless exactly true)
   "Unchanged since saved" | "Verified", "Authentic", "Proven"
   "Changed or unreadable since saved" | "Tampered", "Forged"
   "File missing" |
   "Not checked yet" |
   "Received by ShieldHer" and "You entered" | "Created on", "Sent on"
   "Evidence deleted" |
   "Preview: not working yet" and "Coming in the next version" (Account Check) | any result, number, or the words "fake" or "bot"
3. Four fixed text blocks, written EXACTLY as below:
   Block 1, what "Unchanged since saved" means: "The saved copy is identical to what ShieldHer received. It does not show who made it, when it was originally made, or whether it was edited before you shared it. It does not decide whether it can be used in a legal process."
   Block 2, time: "'Received by ShieldHer' comes from this phone's clock. 'You entered' is a date you typed yourself."
   Block 3, delete: "This removes the file and its record from this phone. It cannot be undone. Case notes that mention it stay, marked 'evidence deleted'."
   Block 4, storage (Settings): "Evidence files are stored encrypted, with keys held in the Android Keystore. Case notes are stored encrypted in a separate file. The vault's technical record of each item (receipt hash, receipt time, time zone, source label, file size and file path) is kept in the app's private storage in a database that is not encrypted."
4. A note: "Do not add any sentence about Android backup until FE-0.3 confirms allowBackup=false."
5. Review rule: "Abdul reviews every new string against this guide before a pull request merges."

FINISH: `git status --short` must show only docs/wording_guide.md modified; `git diff main --stat` must show changes in that file only. Commit with the message above and run `git push -u origin fe/p1-wording-guide`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules.
```

---

## GROUP C: after B1 to B3 are merged

### Prompt C1. FE-1.2 support: Screen specs for the wireframes

**Branch:** `fe/p1-screen-specs`
**Commit:** `FE-1.2: screen specs for wireframes`
**Run after:** the design basis and wording guide are merged, so the specs use the final colours and wording.

```text
TASK: FE-1.2 support. Write docs/wireframes/screen_specs.md, a text specification for each screen that Gotam will use to draw the Figma wireframes. Documentation only. Do not invent features.

BRANCH: after the start condition passes, run `git switch -c fe/p1-screen-specs`.

INPUTS (read-only; STOP and tell me if any is missing): docs/stage3-decisions.md; docs/design_basis.md; docs/wording_guide.md; the v2 plan and the phases file in docs/stage3/ (use their screen table and the FE-4.x deliverable rows for each screen's content and states).

STEP 1: WRITE docs/wireframes/screen_specs.md
For each screen below, give: purpose (one sentence); how the user arrives and where each action leads; the elements on the screen (a short list); the data source (real vault, case store, in-memory session, or static); EVERY state (the v2 screen table lists them: for example not checked, unchanged, changed, missing, preview unavailable, delete confirm, loading, empty, error); and the exact on-screen strings, taken from docs/wording_guide.md where they exist. Mark any string you had to propose as "PROPOSED: needs Abdul's review".
Screens: 1 Home; 2 Evidence list (with search by source and date); 3 Evidence detail (full hash with copy button, "Received by ShieldHer" with time zone, source, MIME type, stored size labelled as encrypted size, in-memory preview, Verify, linked cases, add to case, delete); 4 Import evidence; 5 Cases list and create; 6 Case timeline (ordered by "You entered", falling back to "Received by ShieldHer", each item labelled with which; add evidence, add note; "evidence deleted" display); 7 Settings and privacy (item count, screenshot protection toggle, storage text Block 4, delete all, "What ShieldHer cannot prove" page; NO app-lock row); 8 Demo mode (debug builds only: "Demo data" banner, reset); 9 Onboarding (max 3 pages); 10 Account Check (layout only: grey "Preview: not working yet" label, platform selector, followers, following, bio text, link yes/no, a button that shows only "Coming in the next version", nothing typed is stored; NO result, NO numbers, NO result colours).
Also add: a navigation map (bottom navigation Home, Evidence, Cases, More; where Settings, Help and Account Check sit under More); a list of the shared components each screen uses (StatusBadge, EvidenceCard, SectionHeader, EmptyState, ErrorState, ConfirmDialog); and a short "Wireframe checklist": every screen drawn at phone width, every state drawn at least once, every status shows icon plus text plus colour, and the tint colours come from docs/design_basis.md.

FINISH: `git status --short` must show only docs/wireframes/screen_specs.md as new. Commit with the message above and run `git push -u origin fe/p1-screen-specs`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the list of strings marked PROPOSED.
```

---

### FE-1.2: Wireframes in Figma (human, Gotam)

1. Use `docs/wireframes/screen_specs.md` and `docs/design_basis.md`.
2. Draw screens 1 to 7 at phone width, including every state (empty, loading, error, each badge, delete confirm). Draw 8 to 10 only if time allows.
3. Use the exact colours, badge tints and icon names from the design basis. Every status shows icon plus text plus colour.
4. Share the Figma link with the team and put it in `docs/wireframes/README.md` (a one-line file is fine).
5. **Freeze on Tue 17 Nov.** Later changes need a written note to the team.

### FE-1.5: Wireframe check (optional, Abdul)

Show the wireframes to 3 people outside the team, about 10 minutes each. Ask them to point out where they would tap to save evidence and what "Unchanged since saved" means to them. Write the top issues in `docs/wireframes/feedback.md`. **Skip this if it threatens the 22 Nov checkpoint.**

---

## After Phase 0 and 1

When everything above is merged:
- Decisions D1 to D17 and DS-1 to DS-4 are recorded, and the screen list is frozen.
- The sweep question is answered, and the build spike proved the new libraries work.
- The Stage 1 baseline is recorded (tests, backup settings, log audit).
- The design basis is corrected and checked by script, the wording guide is fixed, and the wireframes are done.
- The supervisor has been asked to sign off, and the ethics question is raised.

**Next: Phase 2 (vault additions and the case store) and Phase 3 (shell and design system in code).** One task belongs at the start of Phase 2 that the plan doesn't list: Bhoomika publishes the method signatures with **fake repository implementations**, so Gotam can build screens before the real vault code is finished. Without it, the 27 Nov checkpoint is not reachable. I can write the Phase 2 and 3 prompts next.
