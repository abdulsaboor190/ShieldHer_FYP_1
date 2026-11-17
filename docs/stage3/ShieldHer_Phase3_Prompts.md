# ShieldHer Stage 3: Phase 3 Prompts (Shell and Design System in Code)

**Written:** 17 November 2026
**Repository:** https://github.com/abdulsaboor190/ShieldHer_FYP_1.git
**Working folder:** `D:\23i-3039\P2\ShieldHer1\Shieldher` (git root, Android project). The `ml` folder is Stage 2, frozen, in its own repo.
**Phase 3 dates:** Mon 16 Nov to Tue 24 Nov. **Checkpoint Sun 22 Nov:** vault additions done and the shell navigates. **Freeze Thu 3 Dec.**

## What Phase 3 builds

One consistent look and one navigation frame, so every later screen looks like one app: the colour tokens and theme, type and spacing, the strings skeleton, a hard-coding check, `MainActivity` with bottom navigation, the shared components (`StatusBadge`, `EvidenceCard`, empty/error states, dialog, buttons, loading indicator), screenshot protection, a debug-only component gallery, and a `UiState` template.

## Before you start (check these)

- Phases 0 and 1 are merged to `main`. In particular: the FE-0.2 build spike (Navigation, fragment-ktx, lifecycle, coroutines-test and arch-core-testing are in `gradle/libs.versions.toml`), `docs/design_basis.md`, `docs/wording_guide.md`, and `docs/contrast_check.py`.
- **Phase 2 runs in parallel and is not in this file.** Bhoomika builds the vault additions and the case store. Screens (Phase 4) start from fake repositories, so ask her to publish the method signatures and fake implementations early. I can write the Phase 2 prompts next.

---

## Order, owners and branches

| Order | Prompt | Deliverables | Type | Suggested owner | Branch |
|---|---|---|---|---|---|
| 0 | P3-0 | Import 9 Material icons | Human, then agent check | Gotam | `fe/p3-icons` |
| 1 | P3-1 | FE-3.1a, 3.1b, 3.1c: colours, theme, type and spacing | Agent | Gotam | `fe/p3-theme` |
| 2 | P3-2 | FE-3.7 strings skeleton + FE-3.1d token-check script | Agent | Abdul | `fe/p3-strings-tokens` |
| 3a | P3-3 | FE-3.2a, 3.2b: `MainActivity`, navigation, placeholders | Agent + Human check | Gotam | `fe/p3-shell` |
| 3b | P3-4 | FE-3.6: `UiState` and test helpers | Agent | Abdul | `fe/p3-uistate` |
| 4 | P3-5 | FE-3.3a, 3.3b: `StatusBadge`, `EvidenceCard` | Agent | Gotam | `fe/p3-badge-card` |
| 5 | P3-6 | FE-3.3c, 3.3d: states, dialog, buttons, loading | Agent | Gotam | `fe/p3-states-dialog` |
| 6 | P3-7 | FE-3.4: screenshot protection | Agent | Abdul | `fe/p3-screen-protection` |
| 7 | P3-8 | FE-3.5: component gallery (debug only) | Agent + Human check | Gotam or Abdul | `fe/p3-gallery` |

**Merge order matters because the prompts share `strings.xml`, `dimens.xml` and `styles.xml`.** Run P3-0, P3-1 and P3-2 in that order and merge each before the next. After P3-2, two lanes can run side by side:
- **Lane A (shell):** P3-3, then P3-7.
- **Lane B (components):** P3-5, P3-6, then P3-8.
- **Anytime after P3-1:** P3-4.

Gotam owns most of these, which is the biggest schedule risk. Moving P3-2, P3-4 and P3-7 to Abdul takes about a third of Phase 3 off him.

---

## How to run each prompt

```powershell
cd D:\23i-3039\P2\ShieldHer1\Shieldher
git switch main
git pull
git status --short          # must print nothing
```

1. Paste the **Standing rules** (below), then the prompt.
2. Read the report and run the **"You check"** list.
3. The agent commits and pushes **its own task branch**. **You** open the pull request, get a teammate to review it, and merge it. Then `git switch main`, `git pull`, and confirm `git status --short` is empty.

---

## Standing rules (paste at the top of every prompt)

```text
STANDING RULES (apply to the whole task)
1. Work ONLY inside the git root D:\23i-3039\P2\ShieldHer1\Shieldher. Do not open, edit or create anything in the sibling `ml` folder or the outer folder. NEVER search outside this repo: if a file is missing, STOP and say so.
2. Files under app/src/main/java/com/shieldher/vault are Stage 1 code. Do NOT edit, move or delete them.
3. START CONDITION: run `git status --short` and `git branch --show-current`. The tree must be clean and the branch must be `main`. Run `git pull`. If not, STOP and tell me. NEVER create a branch from any branch other than an up-to-date main.
4. EDIT ONLY the files this prompt names. For existing resource files (colors.xml, strings.xml, dimens.xml, styles.xml, themes.xml): APPEND ONLY, except where the prompt names a specific item to modify. Never delete or rename an existing resource that Stage 1 code or layouts use (search for usages first).
5. Do NOT edit any Gradle file or add a dependency. If a library you need is missing, STOP and tell me.
6. No hard-coded colours, sizes or user-facing text in layouts or Kotlin: use @color/sh_* , @dimen/sh_* and @string/* . The only literal dimensions allowed are 0dp, match_parent and wrap_content. Text sizes are defined once in dimens.xml.
7. Never use DynamicColors. The app is Light theme only (decision DS-3).
8. Never start an emulator. Never run connectedDebugAndroidTest. Run installDebug ONLY if this prompt gives an emulator serial, and only with `$env:ANDROID_SERIAL` set to it. Never install on a phone.
9. GIT PERMISSIONS: you MAY create the branch named in the prompt, commit on it (one commit per deliverable, messages given in the prompt), and run `git push -u origin <that branch>` at the end. You must NOT merge, rebase, force-push, tag, change git config, or push to main.
10. Never print secrets (keystores, passwords, tokens, the contents of local.properties). Use PowerShell syntax.
11. If anything conflicts with these rules or surprises you, STOP and tell me before continuing.

REPORT BACK (every task)
- Files created or changed (full paths) and files skipped
- Commands run and their key output
- Output of `git status --short`, `git diff main --stat` and `git log --oneline -6`
- Build and unit-test results (number of tests passed)
- Anything surprising
```

---

## P3-0. Import nine Material icons [HUMAN, then agent check]

**Why:** the status badges and the bottom navigation need nine vector icons. Please import them with Android Studio's Vector Asset Studio. An agent writing icon path data from memory risks drawing the wrong shape.
**Branch:** `fe/p3-icons`

**Human steps** (from a clean, updated `main`):

```powershell
cd D:\23i-3039\P2\ShieldHer1\Shieldher
git switch main
git pull
git switch -c fe/p3-icons
```

In Android Studio: right-click `app/src/main/res/drawable` → **New → Vector Asset** → **Clip Art** → search the Material icon name → set the file name → **Next → Finish**. Do this for each row. Leave the default size and tint:

| Material icon | File name | Used for |
|---|---|---|
| `warning` | `ic_status_changed` | Changed or unreadable since saved |
| `cancel` | `ic_status_missing` | File missing |
| `schedule` | `ic_status_not_checked` | Not checked yet |
| `delete` | `ic_status_deleted` | Evidence deleted |
| `visibility` | `ic_status_preview` | Preview: not working yet |
| `home` | `ic_nav_home` | Bottom navigation |
| `folder` | `ic_nav_evidence` | Bottom navigation |
| `assignment` | `ic_nav_cases` | Bottom navigation |
| `more_horiz` | `ic_nav_more` | Bottom navigation |

(`ic_status_unchanged` already exists from FE-E.10.) Then paste this to the agent:

```text
TASK: P3-0 Verify the nine imported Material icons and record them. I created the branch fe/p3-icons myself and imported the icons through Vector Asset Studio.

START CONDITION (replaces the main-branch rule for this task only): the branch must be fe/p3-icons and `git status --short` must show ONLY the new icon files under app/src/main/res/drawable. Report anything else and stop. Do not create another branch.

STEP 1: VERIFY (read-only)
For each of these files in app/src/main/res/drawable: ic_status_changed, ic_status_missing, ic_status_not_checked, ic_status_deleted, ic_status_preview, ic_nav_home, ic_nav_evidence, ic_nav_cases, ic_nav_more, confirm it exists, is valid XML, and its root element is <vector>. Also confirm ic_status_unchanged.xml is still present and unchanged. Report any that are missing. The six status icons must be visually distinct files (compare their pathData and confirm no two status icons have identical pathData; in particular ic_status_changed and ic_status_missing must differ). Do not edit any icon.
Run `.\gradlew.bat assembleDebug` and report the result.

STEP 2: RECORD
Append to docs/design_basis.md (keep everything already there) a line under the existing "Icons" section listing the icons now present, with their roles (use the table above). Change nothing else.

FINISH: `git status --short` shows only the nine icon files and docs/design_basis.md. Commit with the message "P3-0: import Material status and navigation icons" and run `git push -u origin fe/p3-icons`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules.
```

**You check:** nine new files plus one doc change, the build passes, and no two status icons look alike.

---

## P3-1. FE-3.1a, 3.1b, 3.1c: Colours, theme, type and spacing

**Branch:** `fe/p3-theme`
**Commits:** `FE-3.1a: colour tokens`, `FE-3.1b: Material 3 light theme`, `FE-3.1c: type, spacing and shape resources`
**Why:** every later screen builds on this. A wrong colour here spreads everywhere.

```text
TASK: P3-1 Colour tokens (FE-3.1a), Material 3 Light theme (FE-3.1b), type/spacing/shape resources (FE-3.1c). Resource files only; no Kotlin changes except none.

BRANCH: after the start condition passes, run `git switch -c fe/p3-theme`.

STEP 1: INSPECT (read-only; report before changing anything)
a) List every file in app/src/main/res/values and values-night. Report the existing theme name(s) and the parent, and which theme the manifest sets on <application>.
b) List all existing color names in colors.xml and, for EACH, how many times it is used in app/src/main (layouts, themes, Kotlin). Do not remove any that are used.
c) Report whether styles.xml and dimens.xml already exist.
d) Search the whole repo for "DynamicColors" and report any hits.
e) Read docs/design_basis.md and use it as the source of truth for colours and the M3 role mapping.
Show me this report, then continue.

STEP 2 (commit "FE-3.1a: colour tokens"): APPEND to app/src/main/res/values/colors.xml, under an XML comment "ShieldHer design tokens", the following named colours (do not change or remove existing ones):
sh_white #FFFFFF; sh_black #000000; sh_primary #164E50; sh_on_primary #FFFFFF; sh_primary_container #E4F0E9; sh_on_primary_container #164E50; sh_secondary #267878; sh_on_secondary #FFFFFF; sh_secondary_container #A8D5C8; sh_on_secondary_container #164E50; sh_accent #55A6A0 (comment: DECORATION ONLY, never text, icons, loading or selected states); sh_mint #A8D5C8; sh_surface_light #E4F0E9; sh_background #F8FAF7; sh_card #FFFFFF; sh_text #263635; sh_text_secondary #5A6662; sh_outline #75817D; sh_outline_variant #A8D5C8; sh_error #AD464E; sh_on_error #FFFFFF; sh_error_container #F8E8E9; sh_on_error_container #AD464E;
status colours: sh_status_success_fill #38836B; sh_status_success_text #2F6F5A; sh_tint_success #E6F2EE; sh_status_warning_fill #D69A42; sh_status_warning_text #8F5E12; sh_tint_warning #FBF1E0; sh_status_neutral_fill #898D95; sh_status_neutral_text #666A72; sh_tint_neutral #ECEDEF; sh_status_error_fill #C45B62; sh_status_error_text #AD464E; sh_tint_error #F8E8E9.
Add a comment above the status block: fill colours are for tinted backgrounds only; icons and text use the *_text colours.

STEP 3 (commit "FE-3.1b: Material 3 light theme"): MODIFY the existing application theme in app/src/main/res/values/themes.xml IN PLACE (keep its name so the manifest does not change):
a) Parent: Theme.Material3.Light.NoActionBar (NOT a DayNight parent).
b) Set these items using the sh_* colours: colorPrimary, colorOnPrimary, colorPrimaryContainer, colorOnPrimaryContainer, colorSecondary, colorOnSecondary, colorSecondaryContainer, colorOnSecondaryContainer, colorTertiary=sh_accent, colorOnTertiary=sh_text, colorTertiaryContainer=sh_secondary_container, colorOnTertiaryContainer=sh_on_secondary_container, android:colorBackground=sh_background, colorOnBackground=sh_text, colorSurface=sh_card, colorOnSurface=sh_text, colorSurfaceVariant=sh_surface_light, colorOnSurfaceVariant=sh_text_secondary, colorOutline=sh_outline, colorOutlineVariant=sh_outline_variant, colorSurfaceContainerLowest=sh_card, colorSurfaceContainerLow=sh_background, colorSurfaceContainer=sh_background, colorSurfaceContainerHigh=sh_surface_light, colorSurfaceContainerHighest=sh_surface_light, colorSurfaceInverse=sh_text, colorOnSurfaceInverse=sh_background, colorPrimaryInverse=sh_secondary_container, colorError, colorOnError, colorErrorContainer, colorOnErrorContainer, colorSurfaceDim=sh_surface_light, colorSurfaceBright=sh_card, colorSurfaceTint=sh_primary. If any attribute does not exist in the Material version used by the project, skip it and report which one.
c) Add android:forceDarkAllowed=false, android:statusBarColor=@color/sh_primary and android:windowLightStatusBar=false. (On Android 15 and later the status bar colour is ignored and the app draws edge to edge; the shell handles that in P3-3.)
d) If app/src/main/res/values-night/themes.xml exists, change its application theme to be the SAME Light theme (same parent and same items), so the app never appears half dark in system dark mode. Do not delete the file.
e) Do not call or reference DynamicColors anywhere.
f) Apply the theme only to the existing application-level theme item names; do not edit any Stage 1 layout.

STEP 4 (commit "FE-3.1c: type, spacing and shape resources"):
a) Create or APPEND to res/values/dimens.xml (proposed defaults; confirm at FE-3.1c review): sh_space_4 4dp; sh_space_8 8dp; sh_space_12 12dp; sh_space_16 16dp; sh_space_24 24dp; sh_space_32 32dp; sh_radius_card 12dp; sh_radius_button 8dp; sh_touch_min 48dp; sh_icon_small 16dp; sh_icon_medium 24dp; sh_text_title 22sp; sh_text_heading 18sp; sh_text_body 16sp; sh_text_body_small 14sp; sh_text_caption 12sp.
b) Create or APPEND to res/values/styles.xml: five text appearances: ShieldHer.TextAppearance.Title (sh_text_title, bold, sh_text), .Heading (sh_text_heading, medium weight, sh_text), .Body (sh_text_body, sh_text), .BodySmall (sh_text_body_small, sh_text_secondary), .Caption (sh_text_caption, medium weight, sh_text_secondary). Use Material 3 text appearances as parents. Body text must never be below 14sp.
c) Add shape appearances ShieldHer.ShapeAppearance.Card (rounded, corner size @dimen/sh_radius_card) and ShieldHer.ShapeAppearance.Button (rounded, @dimen/sh_radius_button). Do NOT yet define button, card, badge or loading styles (those are FE-3.3).

STEP 5: VERIFY
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests passed.
b) Search the repo again for "DynamicColors" and confirm no new hits.
c) `git diff main --stat`: only res/values files changed; for colors.xml, strings of existing colours unchanged.
d) Run `py -3 docs/contrast_check.py` and confirm exit code 0 (colours in the design basis are unchanged).

FINISH: three commits as named, then `git push -u origin fe/p3-theme`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the Step 1 report and any theme attribute you skipped.
```

**You check:**
- Only `res/values*/` files changed (colors, themes, dimens, styles), and the existing colour names are still there.
- The build and unit tests pass.
- **Human visual check (optional now, required before Phase 3 closes):** run the app on the emulator and open any Stage 1 screen. It should show the teal theme and no purple. A gallery check follows in P3-8.

---

## P3-2. FE-3.7 and FE-3.1d: Strings skeleton and token-check script

**Branch:** `fe/p3-strings-tokens`
**Commits:** `FE-3.7: strings skeleton`, `FE-3.1d: token check script`
**Why:** every user-facing sentence lives in one reviewed file, and a script catches hard-coded colours, sizes and text before they merge.

```text
TASK: P3-2 Strings skeleton (FE-3.7) and the hard-coding check script (FE-3.1d).

BRANCH: after the start condition passes, run `git switch -c fe/p3-strings-tokens`.

STEP 1 (commit "FE-3.7: strings skeleton"): APPEND to app/src/main/res/values/strings.xml under an XML comment "ShieldHer dashboard strings" (never change or remove existing strings):
a) Read docs/wording_guide.md. The four fixed blocks must be copied EXACTLY from it, with XML escaping (an apostrophe becomes \' and an ampersand becomes &amp;).
b) Add these keys and text. Text taken from the wording guide is final; anything marked PROPOSED must carry an XML comment "<!-- PROPOSED: needs Abdul's review -->" on the line above it.
  nav_home "Home"; nav_evidence "Evidence"; nav_cases "Cases"; nav_more "More"
  status_unchanged "Unchanged since saved"; status_changed "Changed or unreadable since saved"; status_missing "File missing"; status_not_checked "Not checked yet"; status_deleted "Evidence deleted"; status_preview "Preview: not working yet"
  label_received "Received by ShieldHer"; label_you_entered "You entered"
  evidence_received_at_format "Received by ShieldHer: %1$s" (PROPOSED)
  block_unchanged_meaning = fixed block 1; block_time = fixed block 2; dialog_delete_message = fixed block 3; settings_storage_text = fixed block 4
  dialog_delete_title "Delete this evidence?" (PROPOSED); common_delete "Delete"; common_cancel "Cancel"; common_ok "OK"; common_retry "Try again" (PROPOSED)
  empty_generic "Nothing here yet." (PROPOSED); error_generic "Something went wrong. Please try again." (PROPOSED)
  account_check_preview_label = same text as status_preview; account_check_coming_soon "Coming in the next version"
  placeholder_screen_note "This screen is coming soon." (PROPOSED)
c) Do not add any sentence about Android backup (the wording guide forbids it until FE-0.3 confirms allowBackup=false).

STEP 2 (commit "FE-3.1d: token check script"): create scripts/check_tokens.py (Python 3, standard library only) and scripts/token_check_stage1_layouts.txt.
a) scripts/token_check_stage1_layouts.txt: the list of file names of every layout XML under app/src/main/res/layout* that exists RIGHT NOW (these are Stage 1 layouts and are excluded from the check).
b) scripts/check_tokens.py checks every layout XML under app/src/main/res/layout* NOT in that exclusion list for: (1) hex colour literals (#RGB, #RRGGBB, #AARRGGBB); (2) android:text, android:hint, android:contentDescription, android:title or app:title values that do not start with "@" (tools: attributes are ignored); (3) any dimension literal in dp or sp other than 0dp (values like 8dp, 14sp); (4) hex literals or dp/sp literals in drawable XML are NOT checked.
c) It also scans Kotlin files under app/src/main/java/com/shieldher/dashboard for Color.parseColor(, Color.rgb( and Color.argb( and reports each.
d) It also checks styles.xml/themes.xml/dimens.xml are NOT scanned for literals (tokens are defined there). Directories app/src/debug and app/src/test are not scanned.
e) Print one line per violation (file:line: rule: text) and a summary; exit non-zero if any violation exists.
f) Run `py -3 scripts/check_tokens.py` and report the output and exit code. It must pass on the current code (no dashboard layouts exist yet).
g) Append a short "Token check" entry to docs/commands.md under the Build and test section: `py -3 scripts/check_tokens.py` and what it checks. Keep everything else in that file.

STEP 3: VERIFY
`.\gradlew.bat assembleDebug testDebugUnitTest` must pass. `git diff main --stat`: strings.xml additions only, plus the two scripts files and docs/commands.md.

FINISH: two commits as named, then `git push -u origin fe/p3-strings-tokens`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the list of strings marked PROPOSED and the token-check output.
```

**You check:** strings were only added (existing strings untouched), the four fixed blocks match the wording guide word for word, and the token check exits 0. Abdul reviews the PROPOSED strings before merging.

---

## P3-3. FE-3.2a and 3.2b: Shell and navigation [AGENT + HUMAN CHECK]

**Branch:** `fe/p3-shell`
**Commits:** `FE-3.2a: MainActivity and launcher`, `FE-3.2b: navigation graph and bottom navigation`
**Needs merged first:** P3-0 (icons), P3-1 (theme), P3-2 (strings).
**Optional emulator step:** start one emulator from Android Studio and note its serial (`adb devices -l`). If you skip it, delete Step 5 from the prompt.

```text
TASK: P3-3 The app shell: MainActivity as the launcher, a navigation graph, bottom navigation and four placeholder screens. Emulator serial for the optional install step: <EMULATOR_SERIAL> (it must start with "emulator-").

BRANCH: after the start condition passes, run `git switch -c fe/p3-shell`.

EXCEPTION TO THE STANDING RULES (this task only): you MAY edit app/src/main/AndroidManifest.xml (only as described in Step 2) and create files under app/src/main/java/com/shieldher/dashboard and app/src/main/res (layout, menu, navigation). You may NOT edit Stage 1 Kotlin files, including IntakeActivity and EvidenceListActivity.

STEP 1: INSPECT (read-only; report first)
a) Print the <application> and all <activity> entries of app/src/main/AndroidManifest.xml, showing which activity currently has the MAIN/LAUNCHER intent filter and what IntakeActivity's intent filters are.
b) Confirm view binding is enabled and the Navigation, fragment-ktx and Material libraries exist in gradle/libs.versions.toml. If not, STOP.
c) Confirm these drawables exist: ic_nav_home, ic_nav_evidence, ic_nav_cases, ic_nav_more. If not, STOP.

STEP 2 (commit "FE-3.2a: MainActivity and launcher"):
a) Create res/layout/activity_main.xml: a vertical LinearLayout (id root, background @color/sh_background) containing, in order: a View (id status_bar_spacer, height 0dp initially, background @color/sh_primary); a FragmentContainerView (id nav_host, NavHostFragment, defaultNavHost true, height 0dp with layout_weight 1); a BottomNavigationView (id bottom_nav, menu @menu/bottom_nav_menu). No literal dimensions other than 0dp, match_parent and wrap_content.
b) Create app/src/main/java/com/shieldher/dashboard/MainActivity.kt (AppCompatActivity, view binding). The targetSdk is 37, so the app draws edge to edge and content can sit under the status bar: in onCreate set a window-insets listener on the root that sets the status_bar_spacer height to the top system-bar inset and applies the left/right system-bar insets as padding on the root. Do NOT add bottom padding on the root (BottomNavigationView handles the bottom inset). Do not call DynamicColors and do not use Compose.
c) Manifest: register MainActivity with android:exported="true" and the MAIN/LAUNCHER intent filter. REMOVE the MAIN/LAUNCHER intent-filter from EvidenceListActivity (keep that activity registered, otherwise unchanged). Do NOT touch IntakeActivity or its intent filters.

STEP 3 (commit "FE-3.2b: navigation graph and bottom navigation"):
a) res/menu/bottom_nav_menu.xml with four items, ids and icons: home (@drawable/ic_nav_home, title @string/nav_home), evidence (ic_nav_evidence, nav_evidence), cases (ic_nav_cases, nav_cases), more (ic_nav_more, nav_more).
b) res/navigation/main_nav_graph.xml with four fragment destinations whose ids match the menu item ids (home, evidence, cases, more) and startDestination home. Use fully qualified fragment class names.
c) Four placeholder fragments with a shared layout res/layout/fragment_placeholder.xml (a centred TextView for the title, a second TextView for @string/placeholder_screen_note, both using the ShieldHer text appearances): com.shieldher.dashboard.home.HomeFragment, com.shieldher.dashboard.evidence.EvidenceListFragment, com.shieldher.dashboard.cases.CasesListFragment, com.shieldher.dashboard.settings.MoreFragment. Each sets its title from the matching nav_* string.
d) In MainActivity connect the bottom navigation to the NavController (setupWithNavController).

STEP 4: VERIFY
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests passed.
b) Run `py -3 scripts/check_tokens.py` (it exists after P3-2); it must exit 0.
c) Read the MERGED manifest (app\build\intermediates\merged_manifests\debug... ) and report: which activity has MAIN/LAUNCHER (it must be MainActivity only) and that IntakeActivity still has its SEND intent filters unchanged.
d) `git diff main --stat`: no Stage 1 Kotlin file changed; the manifest diff shows only MainActivity added and the LAUNCHER filter removed from EvidenceListActivity.

STEP 5 (only if I gave a serial): set `$env:ANDROID_SERIAL="<EMULATOR_SERIAL>"`, confirm the serial starts with "emulator-", run `.\gradlew.bat installDebug`, launch with `adb -s <EMULATOR_SERIAL> shell monkey -p com.shieldher -c android.intent.category.LAUNCHER 1`, and report the resumed activity from `adb -s <EMULATOR_SERIAL> shell dumpsys activity activities` (the line containing mResumedActivity). It must be MainActivity.

FINISH: two commits as named, then `git push -u origin fe/p3-shell`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the Step 1 report, the merged-manifest findings and the resumed activity.
```

**You check (on the emulator, by hand):**
1. The app opens to **Home** with four tabs, and each tab shows its placeholder.
2. The top bar area is deep teal and the status icons are readable, with nothing hidden under the status bar or the bottom gesture area.
3. **Sharing a picture from the Gallery to ShieldHer still saves it** (the Stage 1 share path). This is the Phase 3 acceptance test.
4. No purple anywhere.

---

## P3-4. FE-3.6: `UiState` and ViewModel test template

**Branch:** `fe/p3-uistate`
**Commit:** `FE-3.6: UiState and ViewModel test template`
**Needs merged first:** P3-1 only. This can run in parallel with the shell.

```text
TASK: P3-4 UiState and a ViewModel test template.

BRANCH: after the start condition passes, run `git switch -c fe/p3-uistate`.

STEP 1: CREATE app/src/main/java/com/shieldher/dashboard/common/UiState.kt:
a sealed interface UiState<out T> with: object Loading; object Empty; data class Content<T>(val data: T); data class Error(@StringRes val messageRes: Int, val canRetry: Boolean = true). Add a short KDoc explaining that every screen exposes one StateFlow<UiState<...>> and renders exactly these four states (loading, empty, content, error).

STEP 2: CREATE test helpers and the template:
a) app/src/test/java/com/shieldher/dashboard/testing/MainDispatcherRule.kt: a JUnit rule that sets Dispatchers.setMain(StandardTestDispatcher or UnconfinedTestDispatcher) before each test and resets it after.
b) app/src/test/java/com/shieldher/dashboard/common/UiStateTemplateTest.kt: define a tiny TemplateViewModel inside the test file that loads a List<String> from a fake source and exposes a StateFlow<UiState<List<String>>>. Write tests that show the pattern: starts in Loading; a non-empty list produces Content; an empty list produces Empty; a failing source produces Error with canRetry=true; retry after a failure reaches Content. Use kotlinx-coroutines-test (runTest) and the MainDispatcherRule. Do not use real Android classes that need an emulator.
c) The fake data must be obviously synthetic ("example item 1"). No real evidence data.

STEP 3: VERIFY
`.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests passed (previous count plus your new tests). `py -3 scripts/check_tokens.py` must exit 0 if it exists on main. `git diff main --stat`: only the three new files.

FINISH: commit with the message above and run `git push -u origin fe/p3-uistate`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the unit-test count.
```

---

## P3-5. FE-3.3a and 3.3b: `StatusBadge` and `EvidenceCard`

**Branch:** `fe/p3-badge-card`
**Commits:** `FE-3.3a: StatusBadge`, `FE-3.3b: EvidenceCard`
**Needs merged first:** P3-0, P3-1, P3-2.

```text
TASK: P3-5 The StatusBadge and EvidenceCard components.

BRANCH: after the start condition passes, run `git switch -c fe/p3-badge-card`.

EXCEPTION TO THE STANDING RULES (this task only): you may APPEND new items to dimens.xml and styles.xml under a comment "FE-3.3 components", and add the string evidence_received_at_format if it is missing.

SOURCE OF TRUTH: docs/design_basis.md (state-to-colour-and-icon mapping, badge tints) and docs/wording_guide.md. Read both first.

STEP 1 (commit "FE-3.3a: StatusBadge"):
a) Create app/src/main/java/com/shieldher/dashboard/common/BadgeStatus.kt: an enum with these six entries and the resources each uses (no hex values in Kotlin):
  UNCHANGED: label R.string.status_unchanged, icon R.drawable.ic_status_unchanged, tint R.color.sh_tint_success, content colour R.color.sh_status_success_text
  CHANGED: R.string.status_changed, R.drawable.ic_status_changed, R.color.sh_tint_error, R.color.sh_status_error_text
  MISSING: R.string.status_missing, R.drawable.ic_status_missing, R.color.sh_tint_error, R.color.sh_status_error_text
  NOT_CHECKED: R.string.status_not_checked, R.drawable.ic_status_not_checked, R.color.sh_tint_neutral, R.color.sh_status_neutral_text
  DELETED: R.string.status_deleted, R.drawable.ic_status_deleted, R.color.sh_tint_neutral, R.color.sh_status_neutral_text
  PREVIEW: R.string.status_preview, R.drawable.ic_status_preview, R.color.sh_tint_neutral, R.color.sh_status_neutral_text
b) Create res/layout/view_status_badge.xml (a horizontal layout with an ImageView and a TextView), a drawable res/drawable/bg_status_badge.xml (a rounded rectangle shape whose corner radius is a new dimen sh_radius_badge, proposed 16dp), and the new dimen. Spacing uses sh_space_* dimens; the icon size is sh_icon_small; the text uses ShieldHer.TextAppearance.Caption.
c) Create app/src/main/java/com/shieldher/dashboard/common/StatusBadge.kt: a custom view that inflates the layout and exposes setStatus(status: BadgeStatus). It sets the tinted background (background tint from the tint colour), the icon (tinted with the content colour), the label text (in the content colour), and sets contentDescription to the label text. The badge always shows icon AND text; never colour alone.
d) Create a unit test app/src/test/java/com/shieldher/dashboard/common/BadgeStatusTest.kt (plain JVM, no Robolectric) asserting: all six statuses have distinct label resource ids and distinct icon resource ids; CHANGED and MISSING use different icons (decision DS-1); no resource id is 0; the tint and content colour of every red status (CHANGED, MISSING) are the error ones, of every neutral status the neutral ones, and UNCHANGED uses the success ones.

STEP 2 (commit "FE-3.3b: EvidenceCard"):
a) Create res/layout/view_evidence_card.xml with a MaterialCardView root (card background @color/sh_card, stroke from sh_outline_variant, corner radius sh_radius_card, using the ShieldHer shape appearance) containing: a title TextView (ShieldHer.TextAppearance.Body), a TextView for the received time (ShieldHer.TextAppearance.BodySmall) that uses string evidence_received_at_format, and a StatusBadge.
b) Create app/src/main/java/com/shieldher/dashboard/common/EvidenceCardModel.kt (data class: title: String, receivedAtText: String, status: BadgeStatus) and EvidenceCardView.kt with bind(model: EvidenceCardModel). The formatting of the time is the caller's job; the card only displays the text.
c) A card sits on the off-white background; do not add its own background colour other than @color/sh_card.

STEP 3: VERIFY
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests passed.
b) `py -3 scripts/check_tokens.py` must exit 0 (it scans the new layouts for hex colours, literal dp/sp and literal text).
c) `git diff main --stat`: new dashboard/common files, two layouts, one drawable, dimens/styles additions only.

FINISH: two commits as named, then `git push -u origin fe/p3-badge-card`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the unit-test count and the token-check output.
```

**You check:** `BadgeStatusTest` passes. The card and badge are not visible on screen until the gallery (P3-8) exists.

---

## P3-6. FE-3.3c and 3.3d: States, dialog, buttons, loading

**Branch:** `fe/p3-states-dialog`
**Commits:** `FE-3.3c: section header, empty state, error state`, `FE-3.3d: confirm dialog, button styles, loading indicator`
**Needs merged first:** P3-5 (so it does not conflict on `styles.xml` and `dimens.xml`).

```text
TASK: P3-6 SectionHeader, EmptyState, ErrorState (FE-3.3c) and ConfirmDialog, button styles, loading indicator (FE-3.3d).

BRANCH: after the start condition passes, run `git switch -c fe/p3-states-dialog`.

EXCEPTION TO THE STANDING RULES (this task only): you may APPEND new items to styles.xml, dimens.xml and themes.xml (new overlay themes only) under a comment "FE-3.3 components". You must not change existing items.

SOURCE OF TRUTH: docs/design_basis.md (components, buttons, loading in ocean teal, destructive #AD464E with white text) and docs/wording_guide.md.

STEP 1 (commit "FE-3.3c: section header, empty state, error state"):
a) SectionHeader: a custom TextView class com.shieldher.dashboard.common.SectionHeader styled with ShieldHer.TextAppearance.Heading and spacing from sh_space_* dimens.
b) EmptyStateView (com.shieldher.dashboard.common.EmptyStateView, layout res/layout/view_empty_state.xml): a vertical layout with an ImageView (sh_icon_medium or larger from a new dimen sh_icon_large 48dp, tinted with @color/sh_text_secondary), a message TextView (Body) and an optional action button. API: setContent(@DrawableRes icon: Int, @StringRes message: Int, @StringRes actionText: Int? = null, onAction: (() -> Unit)? = null). Material vector icons only; no illustrations.
c) ErrorStateView (layout res/layout/view_error_state.xml): icon R.drawable.ic_status_changed tinted with @color/sh_status_error_text, a message (default @string/error_generic) and a retry button (@string/common_retry). API: setContent(@StringRes message: Int? = null, canRetry: Boolean, onRetry: (() -> Unit)? = null).

STEP 2 (commit "FE-3.3d: confirm dialog, button styles, loading indicator"):
a) Button styles in styles.xml: ShieldHer.Button.Primary (filled, deep teal sh_primary background, white text, corner radius sh_radius_button, min height sh_touch_min), ShieldHer.Button.Secondary (outlined, sh_primary text and stroke), ShieldHer.Button.Destructive (filled, solid @color/sh_error background, white text, same shape), and ShieldHer.Button.Dialog.Destructive (a text button whose text colour is @color/sh_status_error_text, parent Widget.Material3.Button.TextButton.Dialog).
b) A theme overlay ThemeOverlay.ShieldHer.Dialog.Destructive (parent ThemeOverlay.Material3.MaterialAlertDialog) that sets buttonBarPositiveButtonStyle to ShieldHer.Button.Dialog.Destructive.
c) ShieldHer.Widget.LoadingIndicator in styles.xml: parent Widget.Material3.CircularProgressIndicator, indicatorColor @color/sh_secondary (ocean teal; NOT the soft turquoise accent).
d) ConfirmDialog in com.shieldher.dashboard.common: an object with show(context: Context, @StringRes title: Int, @StringRes message: Int, @StringRes confirmText: Int, destructive: Boolean, onConfirm: () -> Unit). It uses MaterialAlertDialogBuilder, a cancel button using @string/common_cancel, and applies the destructive theme overlay when destructive is true. For a delete, the callers will pass title R.string.dialog_delete_title, message R.string.dialog_delete_message (fixed block 3) and confirm R.string.common_delete.
e) Do not hard-code any colour, size or text in Kotlin or layouts.

STEP 3: VERIFY
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests passed.
b) `py -3 scripts/check_tokens.py` must exit 0.
c) `git diff main --stat`: only new dashboard/common files, the new layouts, and additions to styles.xml, dimens.xml and themes.xml.

FINISH: two commits as named, then `git push -u origin fe/p3-states-dialog`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the unit-test count and the token-check output.
```

**You check:** the destructive style uses `#AD464E`, the loading style uses ocean teal (`sh_secondary`), and nothing uses `sh_accent`.

---

## P3-7. FE-3.4: Screenshot protection

**Branch:** `fe/p3-screen-protection`
**Commit:** `FE-3.4: screenshot protection`
**Needs merged first:** P3-3 (it edits `MainActivity`).

```text
TASK: P3-7 Screenshot protection (FLAG_SECURE), decision D8: on by default, switchable in Settings later, turned off by demo mode.

BRANCH: after the start condition passes, run `git switch -c fe/p3-screen-protection`.

EXCEPTION TO THE STANDING RULES (this task only): you may edit app/src/main/java/com/shieldher/dashboard/MainActivity.kt. You may not edit any Stage 1 file.

STEP 1: CREATE app/src/main/java/com/shieldher/dashboard/common/ScreenProtection.kt:
a) A pure function (no Android types) shouldSecure(settingEnabled: Boolean, demoModeActive: Boolean): Boolean = settingEnabled && !demoModeActive.
b) A small object DemoState with a @Volatile var active: Boolean = false (it is set by demo mode in FE-5.1, debug builds only; nothing sets it yet).
c) A class AppSettings(context) backed by SharedPreferences (file name "shieldher_settings") with a Boolean property screenshotProtectionEnabled, default true. No evidence data, paths, hashes or user text may ever be stored in it. No logging.
d) A function applyTo(window: Window, settings: AppSettings) that sets or clears WindowManager.LayoutParams.FLAG_SECURE using shouldSecure(settings.screenshotProtectionEnabled, DemoState.active).

STEP 2: In MainActivity, call applyTo in onCreate BEFORE setContentView, and again in onResume (so a later settings change takes effect). Do not add any Settings screen UI (that is FE-4.7a).

STEP 3: TEST: app/src/test/java/com/shieldher/dashboard/common/ScreenProtectionTest.kt asserting the four combinations of shouldSecure: (true,false) is true; (true,true) is false; (false,false) is false; (false,true) is false.

STEP 4: VERIFY
`.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests passed. `py -3 scripts/check_tokens.py` must exit 0. `git diff main --stat`: MainActivity.kt modified, ScreenProtection.kt and its test new.

FINISH: commit with the message above and run `git push -u origin fe/p3-screen-protection`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the unit-test count.
```

**You check (by hand on the emulator):** open the app, press the recent-apps button, and the ShieldHer card should be blank. A screenshot of the app should also fail or come out black. (This is part of the FE-5.3 manual checklist as well.)

---

## P3-8. FE-3.5: Component gallery (debug only) [AGENT + HUMAN CHECK]

**Branch:** `fe/p3-gallery`
**Commit:** `FE-3.5: debug-only component gallery`
**Needs merged first:** P3-5 and P3-6.
**Why:** a screen that shows every component on white, off-white and pale mint is the fastest way to catch a wrong colour or a stray purple default before the real screens use them.

```text
TASK: P3-8 A debug-only component gallery. Optional emulator serial for the install step: <EMULATOR_SERIAL> (it must start with "emulator-"; delete Step 4 if I gave none).

BRANCH: after the start condition passes, run `git switch -c fe/p3-gallery`.

EXCEPTION TO THE STANDING RULES (this task only): you may create files under app/src/debug (Kotlin, an AndroidManifest.xml, and res), but you may NOT edit anything under app/src/main. Literal text and dp values are allowed ONLY inside app/src/debug (the gallery is a developer tool, not a user screen), but colours must still come from @color/sh_* resources.

STEP 1: CREATE the gallery
a) app/src/debug/AndroidManifest.xml (merge it if one already exists; keep existing content): declare the activity com.shieldher.dashboard.debug.GalleryActivity with android:exported="true". This file only applies to debug builds, so the gallery cannot reach a release build.
b) app/src/debug/java/com/shieldher/dashboard/debug/GalleryActivity.kt: a scrollable screen, built in code, with three panels stacked vertically, each full width, with these backgrounds: panel 1 @color/sh_card (white), panel 2 @color/sh_background (off-white), panel 3 @color/sh_surface_light (pale mint). Each panel has a heading saying which surface it is, then shows ALL of the following:
  - the six StatusBadge states (UNCHANGED, CHANGED, MISSING, NOT_CHECKED, DELETED, PREVIEW)
  - three EvidenceCard views with different statuses and obviously synthetic text ("Example item", "Received: 16 Nov 2026, 10:00")
  - a SectionHeader
  - an EmptyStateView and an ErrorStateView
  - a CircularProgressIndicator using style ShieldHer.Widget.LoadingIndicator
  - three buttons: primary, secondary, destructive (use the ShieldHer button styles), and a fourth button "Show delete dialog" that opens ConfirmDialog with the delete title, delete message (fixed block 3) and delete confirm strings, destructive = true
  - sample text in each text appearance (Title, Heading, Body, BodySmall, Caption), in main and secondary text colours
c) Do not add any navigation entry to the real app. The gallery is opened only by adb or the Android Studio run configuration.

STEP 2: VERIFY (no emulator needed)
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests passed.
b) `.\gradlew.bat :app:compileReleaseKotlin` must pass. Then confirm GalleryActivity does NOT appear in the release build: search app\build for GalleryActivity under any release path, and check the merged release manifest (app\build\intermediates\merged_manifests\release...) if it exists. Report the findings. If release tasks fail for a reason that also fails on main (for example the 2 known Stage 1 lint errors), report it and do not fix it.
c) `py -3 scripts/check_tokens.py` must exit 0 (it does not scan app/src/debug).
d) `git diff main --stat`: only files under app/src/debug.

STEP 3 (optional, only if I gave a serial): confirm the serial starts with "emulator-", set `$env:ANDROID_SERIAL`, run `.\gradlew.bat installDebug`, then open the gallery with `adb -s <EMULATOR_SERIAL> shell am start -n com.shieldher/com.shieldher.dashboard.debug.GalleryActivity` and report that it started without a crash (check `adb logcat -d -s AndroidRuntime:E` for a crash).

FINISH: commit with the message above and run `git push -u origin fe/p3-gallery`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the release-exclusion findings.
```

**You check (by hand, on the emulator and with your eyes):**
1. Open the gallery: `adb shell am start -n com.shieldher/com.shieldher.dashboard.debug.GalleryActivity`.
2. On **all three backgrounds**: every badge shows an icon, text and a tinted colour, and the two red badges (changed, missing) have **different icons**.
3. **No purple or default Material colours** anywhere (buttons, dialog, cards, loading ring). Any purple means a theme role was missed; send me a screenshot.
4. The delete dialog shows the fixed block 3 wording, with a red "Delete" button.
5. Run **Accessibility Scanner** over the gallery and note any contrast or touch-target warnings.

---

## Phase 3 exit checklist (by Tue 24 Nov)

- [ ] `main` contains P3-0 to P3-8, each merged by pull request.
- [ ] `.\gradlew.bat assembleDebug testDebugUnitTest` passes, and the unit-test count is the previous 8 plus the new tests.
- [ ] `py -3 scripts/check_tokens.py` exits 0 and `py -3 docs/contrast_check.py` exits 0.
- [ ] The app opens to **Home** with four working tabs, and **sharing from the Gallery still saves**.
- [ ] The gallery shows every component on white, off-white and pale mint, with no purple defaults.
- [ ] No use of `DynamicColors`, and no `sh_accent` used for text, icons, loading or selected states.
- [ ] Recents shows a blank card (screenshot protection on).
- [ ] The release build contains no gallery and no spike fragment.
- [ ] Debug-only items (gallery, spike) are the only code outside `app/src/main`.

## After Phase 3

**Phase 4 (the screens)** starts on 20 Nov and runs to 1 Dec, with the core flow due **Fri 27 Nov**. It needs Phase 2: the three vault methods, the case store, the verification session, and the **fake repositories** that let Gotam start before the real code lands. I can write the Phase 2 prompts next, then Phase 4.
