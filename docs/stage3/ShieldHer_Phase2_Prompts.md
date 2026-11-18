# ShieldHer Stage 3: Phase 2 Prompts (Vault Additions, Case Store, Interfaces and Fakes)

**Written:** 18 November 2026
**Repository:** https://github.com/abdulsaboor190/ShieldHer_FYP_1.git
**Working folder:** `D:\23i-3039\P2\ShieldHer1\Shieldher` (git root, Android project). The `ml` folder is Stage 2, frozen, in its own repo.
**Checkpoint:** Sun 22 Nov (vault additions done, shell navigates). **Core flow due:** Fri 27 Nov. **Freeze:** Thu 3 Dec.

## What Phase 2 builds

Everything the screens need behind them, without changing how Stage 1 works:

1. **Three additive vault methods** (decision D5): a three-outcome integrity check, an in-memory preview, and in-app import.
2. **The case store** (D6, D12): an encrypted file holding cases, notes and evidence ids, outside the vault.
3. **A verification session** (D10): an in-memory map of "last checked" results.
4. **Interfaces and fake implementations**, so Phase 4 screens can start before the real code lands.
5. **The proof that Stage 1 is untouched:** zero deleted lines in vault files, and all Stage 1 tests still passing.

`FE-2.8` (`sweepStaleOrphans()`) is **not needed**: the in-flight set is shared across repository instances, so D13 uses option (b). `importFromUri()` calls `sweepOrphans()` before `intake()`, exactly as `IntakeActivity` does.

## Before you start

- Phases 0, 1 and 3 are merged to `main`, including the `fe/p3-fix-tokens` pull request.
- **Instrumented tests need an emulator and your agent cannot start one.** Start one from Android Studio (not a phone) and give the agent its serial where a prompt asks for it. Without one, the agent writes the tests but cannot run them, and **the vault pull request must not merge until they pass on an emulator.**
- **Stage 1 internals are unknown to me.** The first prompt documents the real API surface, and later prompts read it, so nothing is guessed.

---

## Order, owners and branches

| Order | Prompt | Deliverables | Type | Suggested owner | Branch |
|---|---|---|---|---|---|
| 1 | P2-0 | Document the vault API surface (read-only) | Agent | Bhoomika | `fe/p2-vault-surface` |
| 2a | P2-1 | FE-2.0 (interfaces, models, fakes, `ServiceLocator`) + FE-2.6 (`VerificationSession`) | Agent | Abdul | `fe/p2-interfaces` |
| 2b | P2-2 | FE-2.1, 2.2, 2.3: three vault methods, with tests | Agent + Human (emulator) | Bhoomika | `fe/p2-vault-additions` |
| 3 | P2-3 | FE-2.5a, 2.5b, 2.5c: encrypted case store and repository | Agent + Human (emulator) | Abdul | `fe/p2-case-store` |
| 4 | P2-4 | Wire the real implementations into `ServiceLocator` | Agent | Abdul or Gotam | `fe/p2-wire-real` |
| 5 | P2-5 | FE-2.4 and FE-2.7: instrumented regression, additivity, log audit | Agent + Human (emulator) | Bhoomika | `fe/p2-verify` |

**Lanes (run in parallel after P2-0):**
- **Lane D (dashboard data):** P2-1 first (this is the fast one that unblocks Gotam), then P2-3.
- **Lane V (vault):** P2-2, which edits Stage 1 vault files, so it is Bhoomika's and it is reviewed by someone else.
- **Join:** P2-4 needs P2-1, P2-2 and P2-3 merged. P2-5 comes last.

**If you fall behind on 22 Nov, cut in this order:** image preview in `readForDisplay` (keep text, "too large" and "unavailable"), then the optional sections of the case store (`isDemo` flags can wait for FE-5.1 only if the file format version stays 1). Never cut the integrity check, import, or the case store failure tests.

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
3. The agent commits and pushes **its own task branch only**. **You** open the pull request, get a teammate to review it, and merge it on GitHub. (In Phase 3 the agent merged into `main` itself. That skipped review. For Phase 2 the vault changes especially need a second pair of eyes.) Then `git switch main`, `git pull`, and confirm `git status --short` is empty.

---

## Standing rules (paste at the top of every prompt)

```text
STANDING RULES (apply to the whole task)
1. Work ONLY inside the git root D:\23i-3039\P2\ShieldHer1\Shieldher. Do not open, edit or create anything in the sibling `ml` folder or the outer folder. NEVER search outside this repo: if a file is missing, STOP and say so.
2. STAGE 1 VAULT FILES (everything under app/src/main/java/com/shieldher/vault and the existing tests under app/src/androidTest and app/src/test that belong to Stage 1) are ADD-ONLY. Never edit, reformat, rename, move or delete an existing line in them. Only the prompt marked as the vault additions prompt may add code to them, and only by ADDING lines or new files. If a change would require altering or removing an existing line (for example making a private member visible), STOP and tell me instead.
3. START CONDITION: run `git status --short` and `git branch --show-current`. The tree must be clean and the branch must be `main`. Run `git pull`. If not, STOP and tell me. NEVER create a branch from any branch other than an up-to-date main.
4. EDIT ONLY the files this prompt names or creates. Do not edit Gradle files, the manifest, or add a dependency. (The libraries needed already exist: Jetpack Security, coroutines, Room. org.json is part of Android.)
5. No hard-coded colours, sizes or user-facing text in layouts or dashboard Kotlin (use @color/sh_*, @dimen/sh_*, @string/*). This phase has no UI, but the rule still applies to anything that reaches the screen.
6. NEVER log or print evidence content, file names or paths, hashes, URIs, case titles, notes or typed values. No Log.*, println or printStackTrace in new code.
7. Never write decrypted evidence to cache, temp files or external storage. Decrypt to memory only.
8. Never start an emulator. Run `connectedDebugAndroidTest` ONLY if this prompt gives an emulator serial that starts with "emulator-", and only with `$env:ANDROID_SERIAL` set to it. Never run it on a phone. Never run installDebug without ANDROID_SERIAL.
9. GIT PERMISSIONS: you MAY create the branch named in the prompt, commit on it (one commit per deliverable, messages given in the prompt), and run `git push -u origin <that branch>` at the end. You must NOT merge, rebase, force-push, tag, change git config, or push to main.
10. Never print secrets (keystores, passwords, tokens, local.properties). Use PowerShell syntax.
11. If anything conflicts with these rules or surprises you, STOP and tell me before continuing.

REPORT BACK (every task)
- Files created or changed (full paths) and files skipped
- Commands run and their key output
- Output of `git status --short`, `git diff main --stat` and `git log --oneline -6`
- Build and unit-test results (number of tests per class and the total)
- For any prompt that touches the vault: `git diff main --numstat -- app/src/main/java/com/shieldher/vault app/src/androidTest app/src/test` (the deleted-lines column must be 0 for every file)
- Anything surprising
```

---

## P2-0. Document the vault API surface [READ-ONLY]

**Branch:** `fe/p2-vault-surface`
**Commit:** `FE-2.0a: document the Stage 1 vault API surface`
**Why:** the three additions, the interfaces and the case store must fit what Stage 1 really exposes. This also answers an open question from the plan: whether delete-by-id already exists, or would be a fourth vault change.

```text
TASK: P2-0 Document how the Stage 1 vault works today, so the Stage 3 additions can be designed against facts. READ-ONLY on all code. The only file written is docs/stage3/vault_api_surface.md.

BRANCH: after the start condition passes, run `git switch -c fe/p2-vault-surface`.

STEP 1: INVESTIGATE (read-only; never print evidence content, keys or key material)
Write the findings into docs/stage3/vault_api_surface.md with these sections. Label every statement VERIFIED IN CODE (you read it) or INFERENCE (you concluded it).
1. EvidenceRepository: file path; constructor parameters; EVERY public or internal member with its full signature (name, parameter names and types, return type, whether suspend or Flow) and a one-line description.
2. Room entity and DAO: the entity class name, table name, and EVERY field (name, type, nullable). Every DAO method (list, get by id, insert, delete, count, anything else).
3. File store: class name and API (write, read or decrypt, delete, exists, path resolution). How EncryptedFile and MasterKey are built (key alias and encryption scheme only; no key material). The directory the files live in (relative to filesDir) and how file names are derived from ids.
4. intake(): full signature, every parameter, the size limit and where it is enforced, every exception or error type it can throw, how the in-flight set is used, how the hash is computed, and exactly what is stored.
5. verifyIntegrity(): how it works step by step (what it reads, how it hashes, what it compares) and its return value; what it does when the file is missing, truncated, corrupt, or cannot be decrypted. The HashUtil API.
6. Delete: does delete-by-id exist (repository, DAO and file store)? Does deleteAll exist? What does a delete remove (file, row, both) and in what order?
7. Decrypt to memory: is there an existing API that returns decrypted bytes or an InputStream for one item? If not, state exactly what new method would be needed and in which class.
8. Threading and lifecycle: how the repository gets the database (a singleton or one per repository instance), which dispatchers it uses, and how the list is exposed to the UI (Flow or LiveData).
9. Test isolation pattern: the names of the existing androidTest and unit-test classes, how each creates an isolated directory and database (naming, canonical-path check, cleanup in teardown), described in prose with no secrets.
10. Feasibility of FE-2.1 (three-outcome integrity check), FE-2.2 (in-memory preview) and FE-2.3 (in-app import) as PURELY ADDITIVE changes: for each, say yes or no, and list anything that would need a member to change visibility or edit an existing line (which would be a deviation to approve).
11. "Questions for Bhoomika": everything you could not verify.

STEP 2: VERIFY
`git status --short` must show only docs/stage3/vault_api_surface.md as new. `git diff main --stat` must show nothing under app/.

FINISH: commit with the message above and run `git push -u origin fe/p2-vault-surface`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the full text of sections 6, 7 and 10 in the chat.
```

**You check:** nothing under `app/` changed. Bhoomika reads the document and corrects anything wrong, because **every later prompt uses it**. Pay special attention to section 6 (does delete-by-id exist?), 7 (is there a decrypt-to-memory API?) and 10.

---

## P2-1. FE-2.0 and FE-2.6: Interfaces, models, fakes, `ServiceLocator`, `VerificationSession`

**Branch:** `fe/p2-interfaces`
**Commits:** `FE-2.0: data interfaces, models and fakes`, `FE-2.6: VerificationSession`
**Needs merged first:** P2-0.
**Why:** this is the one that unblocks Gotam. Screens are built against these interfaces and the fakes; the real code replaces the fakes later in P2-4. It adds only new files in `dashboard/data` and does not touch the vault.

```text
TASK: P2-1 Define the data interfaces the screens will use, with in-memory fakes, and the in-memory VerificationSession. New files only, under app/src/main/java/com/shieldher/dashboard/data and app/src/test/java/com/shieldher/dashboard/data. Do not touch any vault file.

BRANCH: after the start condition passes, run `git switch -c fe/p2-interfaces`.

INPUTS: read docs/stage3/vault_api_surface.md first (it must exist; if not, STOP). Base the EvidenceItem fields on the REAL Room entity fields in that document: use the same names and types (convert the id to String if it is another type, and tell me). Do not invent fields. If the entity has no time zone field, leave it out and report.

STEP 1 (commit "FE-2.0: data interfaces, models and fakes"): create these files in package com.shieldher.dashboard.data.
a) EvidenceItem.kt: data class with the real entity fields (at least id, source label, mime type, received time in epoch millis, hash as hex text, stored size in bytes; plus time zone id if the entity has it).
b) EvidenceIntegrity.kt: enum UNCHANGED, CHANGED, MISSING. (A later adapter maps the vault's own result onto this.)
c) DisplayContent.kt: sealed interface with Text(val text: String), Image(val bitmap: android.graphics.Bitmap), TooLarge, Unsupported, Unavailable.
d) ImportOutcome.kt: sealed interface with Success(val id: String), TooLarge, Unsupported, Failed. Failed carries no message text (nothing about the file may leak).
e) EvidenceGateway.kt: interface with:
   fun observeEvidence(): Flow<List<EvidenceItem>>
   suspend fun getEvidence(id: String): EvidenceItem?
   suspend fun verify(id: String): EvidenceIntegrity
   suspend fun readForDisplay(id: String, maxBytes: Int): DisplayContent
   suspend fun importFromUri(uri: android.net.Uri, mimeType: String?): ImportOutcome
   suspend fun delete(id: String): Boolean
   suspend fun deleteAll(): Int
f) CaseModels.kt: data class Case(id: String, title: String, description: String, createdAtMillis: Long, isDemo: Boolean = false) and data class CaseItem(id: String, caseId: String, evidenceId: String?, note: String, incidentDateMillis: Long?, addedAtMillis: Long, isDemo: Boolean = false). An item with a null evidenceId is a plain note. incidentDateMillis is the optional "You entered" date.
g) CaseRepository.kt: interface with:
   fun observeCases(): Flow<List<Case>>
   fun observeItems(caseId: String): Flow<List<CaseItem>>
   suspend fun createCase(title: String, description: String): Case
   suspend fun renameCase(caseId: String, title: String)
   suspend fun deleteCase(caseId: String)
   suspend fun addEvidenceItem(caseId: String, evidenceId: String, note: String, incidentDateMillis: Long?): CaseItem
   suspend fun addNoteItem(caseId: String, note: String, incidentDateMillis: Long?): CaseItem
   suspend fun updateItem(itemId: String, note: String, incidentDateMillis: Long?)
   suspend fun removeItem(itemId: String)
   suspend fun casesForEvidence(evidenceId: String): List<Case>
   suspend fun deleteEverything()
   Add KDoc: removing an item or deleting a case NEVER deletes evidence; an item whose evidenceId no longer exists in the vault is shown by the UI as "evidence deleted".
h) BadgeMapping.kt: fun EvidenceIntegrity?.toBadgeStatus(): BadgeStatus (null maps to NOT_CHECKED; UNCHANGED, CHANGED, MISSING map to the same-named BadgeStatus).
i) fake/FakeEvidenceGateway.kt: an in-memory implementation backed by a MutableStateFlow list of 6 obviously synthetic items ("Example screenshot 1" and so on, source "Example app", fixed times). verify() returns UNCHANGED for most ids, CHANGED for one and MISSING for one (deterministic). readForDisplay returns Text("Example preview text") for text mime types and Unavailable otherwise. importFromUri adds a new synthetic item and returns Success. delete removes the item and returns true. No real evidence, no real files.
j) fake/FakeCaseRepository.kt: in-memory implementation with two synthetic cases: one containing an evidence item and a note-only item, and one containing an item whose evidenceId is NOT in the fake gateway (to exercise the "evidence deleted" display). It keeps cases and items in MutableStateFlows and implements every interface method as specified (deleteCase removes its items; removeItem never touches evidence).
k) ServiceLocator.kt: object with var evidenceGateway: EvidenceGateway, var caseRepository: CaseRepository, and val verificationSession: VerificationSession. TEMPORARY: the defaults are the fakes, with a prominent comment "TEMPORARY: P2-4 switches these defaults to the real implementations; fakes stay only for tests and the debug gallery". Add fun installFakes() that resets them to fresh fakes.
l) Unit tests in app/src/test/java/com/shieldher/dashboard/data: FakeCaseRepositoryTest (create, rename, delete case; add evidence item and note item; update and remove item; casesForEvidence; deleteCase does not call or change the fake gateway; deleteEverything), FakeEvidenceGatewayTest (observe, verify returns all three outcomes for different ids, delete, deleteAll, import adds an item), BadgeMappingTest (all four mappings). Use kotlinx-coroutines-test (runTest) and the existing MainDispatcherRule. All fake data must be obviously synthetic.

STEP 2 (commit "FE-2.6: VerificationSession"): create VerificationSession.kt in the same package: data class VerificationRecord(val result: EvidenceIntegrity, val checkedAtMillis: Long); class VerificationSession with fun record(id: String, result: EvidenceIntegrity, checkedAtMillis: Long), fun get(id: String): VerificationRecord?, fun clear(), and val records: StateFlow<Map<String, VerificationRecord>>. In memory only: it must never write to disk or log. Add VerificationSessionTest: a recorded result is returned; a record for another id is separate; clear empties it; a NEW instance starts empty (the "Not checked yet after a restart" rule).

STEP 3: VERIFY
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report the unit tests per class and the total (previously 20).
b) `py -3 scripts/check_tokens.py` must exit 0.
c) `git diff main --stat`: only new files under dashboard/data and its tests; nothing under vault; nothing in any resource or Gradle file.

FINISH: two commits as named, then `git push -u origin fe/p2-interfaces`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the EvidenceItem field list with the entity fields it came from, and the test counts.
```

**You check:** nothing under `vault/` changed, only new files, tests pass. **Then tell Gotam:** he can start Phase 4 screens against `ServiceLocator.evidenceGateway`, `caseRepository` and `verificationSession`.

---

## P2-2. FE-2.1, 2.2, 2.3: The three vault additions [AGENT + HUMAN (emulator)]

**Branch:** `fe/p2-vault-additions`
**Commits:** `FE-2.1: verifyIntegrityDetailed`, `FE-2.2: readForDisplay`, `FE-2.3: importFromUri`
**Needs merged first:** P2-0.
**Run by:** Bhoomika, on her machine. **This is the only prompt that may add code to Stage 1 vault files.** Someone else reviews the pull request commit by commit.
**Human steps for the test run:** start **one emulator** from Android Studio and note its serial (`adb devices -l`). If you skip the serial, delete Step 5 and **do not merge the PR until the new instrumented tests pass on an emulator**.

```text
TASK: P2-2 Add three methods to the Stage 1 vault, purely additively. Emulator serial for the test run: <EMULATOR_SERIAL> (it must start with "emulator-").

BRANCH: after the start condition passes, run `git switch -c fe/p2-vault-additions`.

EXCEPTION TO THE STANDING RULES (this task only): you MAY add code to files under app/src/main/java/com/shieldher/vault/data and app/src/main/java/com/shieldher/vault/files (new methods, new files) and add NEW test files under app/src/androidTest/java/com/shieldher/vault. ADD ONLY: the deleted-lines column of `git diff main --numstat` must be 0 for every Stage 1 file. You may NOT change any existing line, signature, visibility modifier or test. If a change would need that, STOP and tell me what and why (it would be a deviation to approve).

INPUTS: read docs/stage3/vault_api_surface.md first (STOP if missing) and reuse the existing helpers it describes (the hash function, the file store, the entity and DAO). Do not duplicate crypto or hashing code. Follow the test-isolation pattern it describes for every new instrumented test (separate directory name, canonical-path check, cleanup in teardown).

STEP 1 (commit "FE-2.1: verifyIntegrityDetailed"):
a) New file IntegrityResult.kt in the vault data package: enum IntegrityResult { UNCHANGED, CHANGED, MISSING }.
b) New method on EvidenceRepository: suspend fun verifyIntegrityDetailed(id: String): IntegrityResult. UNCHANGED = the item exists, its file exists, decrypts, and the hash equals the stored receipt hash. CHANGED = the file exists but the hash differs, OR it cannot be decrypted or read (corrupt, truncated). MISSING = unknown id, or the file is absent. Leave the existing verifyIntegrity() completely untouched.
c) New instrumented test IntegrityDetailedTest (androidTest, vault package): untouched item gives UNCHANGED; one byte of the stored file flipped gives CHANGED; the stored file truncated gives CHANGED; the stored file deleted gives MISSING; an unknown id gives MISSING. Use the same technique existing Stage 1 tests use to locate and modify the stored file.

STEP 2 (commit "FE-2.2: readForDisplay"):
a) New file EvidenceDisplay.kt: sealed interface with Text(val text: String), Image(val bitmap: android.graphics.Bitmap), TooLarge, Unsupported, Unavailable.
b) New method on EvidenceRepository: suspend fun readForDisplay(id: String, maxBytes: Int = 10_485_760, maxImageSidePx: Int = 1080): EvidenceDisplay. Decrypts to MEMORY ONLY. Unknown id, missing file or decryption failure gives Unavailable. A stored size above maxBytes gives TooLarge, decided BEFORE reading the whole file. MIME image/* is decoded with BitmapFactory in two passes (read the bounds first, then decode with an inSampleSize so the longest side is at most maxImageSidePx) and returned as Image. MIME text/plain is decoded as UTF-8 and returned as Text. Any other MIME gives Unsupported. If the file store has no decrypt-to-memory method, add ONE new additive method to the file store class (and report it as a fourth addition).
c) It must NEVER write a thumbnail, temp file, cache entry or external file, and must not log anything.
d) New instrumented test ReadForDisplayTest: a small generated image previews as Image with the longest side at most the cap; a small text file previews as Text; a file above a small maxBytes gives TooLarge; an unknown id gives Unavailable; a pdf MIME gives Unsupported; and listing filesDir and cacheDir before and after a preview shows NO new files.

STEP 3 (commit "FE-2.3: importFromUri"):
a) New file ImportResult.kt: sealed interface Success(val id: String), TooLarge, Unsupported, Failed.
b) New method on EvidenceRepository: suspend fun importFromUri(uri: android.net.Uri, mimeType: String?): ImportResult. It runs on the IO dispatcher. It calls sweepOrphans() first (the same pre-intake sweep IntakeActivity does, so the removed list-screen sweep is not missed), opens the stream with the context's contentResolver, and calls the EXISTING intake() with source label "In-app import", the same size limit and the same error types as the share path. When mimeType is null use contentResolver.getType(uri) and fall back to whatever intake expects for unknown types. Map the existing intake exceptions onto TooLarge, Unsupported and Failed. The stream is always closed. A failure must leave no ghost row and no orphan file. The uri must never be logged.
c) New instrumented test ImportFromUriTest: importing a small file through a file URI creates one item with source "In-app import"; importing a URI that cannot be opened returns Failed and leaves the item count and the file directory unchanged.

STEP 4: VERIFY (no emulator needed)
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests per class and the total (Stage 1 and Stage 3 unit tests still pass).
b) `.\gradlew.bat assembleDebugAndroidTest` must compile.
c) `git diff main --numstat -- app/src/main/java/com/shieldher/vault app/src/androidTest app/src/test`: report the table. The deleted-lines column must be 0 for every file. Explain every changed (non-new) file.
d) Search the new code for Log., println and printStackTrace; there must be none.

STEP 5 (only if I gave a serial): confirm the serial starts with "emulator-", set `$env:ANDROID_SERIAL="<EMULATOR_SERIAL>"`, run `.\gradlew.bat connectedDebugAndroidTest`, and report passed, failed and skipped. Expected: the 14 Stage 1 tests plus the new ones. If anything fails, copy the failing test names and the first lines of each error. Do NOT fix Stage 1 tests.

FINISH: three commits as named, then `git push -u origin fe/p2-vault-additions`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the numstat table, whether a fourth addition was needed, and the instrumented-test results (or "not run: no serial").
```

**You check:**
- The deleted-lines column is **0 everywhere**, and the only changed (not new) Stage 1 file is `EvidenceRepository.kt` (plus one file-store file, if a fourth addition was needed).
- All 14 Stage 1 instrumented tests still pass, plus the new ones, **on an emulator**.
- The reviewer reads each commit separately.

---

## P2-3. FE-2.5a, 2.5b, 2.5c: Encrypted case store and repository [AGENT + HUMAN (emulator)]

**Branch:** `fe/p2-case-store`
**Commits:** `FE-2.5a: CaseStore encrypted file`, `FE-2.5b: CaseRepository`, `FE-2.5c: case store failure tests`
**Needs merged first:** P2-1 (it implements the `CaseRepository` interface).
**Why:** case notes may describe the harassment. They must be encrypted like the evidence files (decision D12), and a damaged file must never silently reset to empty. This code lives outside the vault, in `dashboard/cases`.
**Human steps for the test run:** an emulator serial, as in P2-2.

```text
TASK: P2-3 The encrypted case store and the real CaseRepository. Emulator serial for the test run: <EMULATOR_SERIAL> (it must start with "emulator-").

BRANCH: after the start condition passes, run `git switch -c fe/p2-case-store`.

EXCEPTION TO THE STANDING RULES (this task only): you may create new files under app/src/main/java/com/shieldher/dashboard/cases and new instrumented tests under app/src/androidTest/java/com/shieldher/dashboard/cases. You may NOT edit any vault file.

INPUTS: read docs/stage3/vault_api_surface.md (how the vault builds EncryptedFile and MasterKey, and the test-isolation pattern) and the interfaces in app/src/main/java/com/shieldher/dashboard/data (Case, CaseItem, CaseRepository). STOP if any is missing.

DESIGN (fixed decisions D6, D12, D14):
- One encrypted file holding the whole store, using Jetpack Security EncryptedFile with a Keystore-backed MasterKey, built the way the vault builds its own, but with a SEPARATE key alias (for example "shieldher_cases_master_key"). No new dependency.
- Content is JSON built with org.json: {"schemaVersion":1,"cases":[...],"items":[...]} where cases and items carry exactly the fields of Case and CaseItem (including isDemo).
- The whole file is kept in memory behind a kotlinx.coroutines Mutex and exposed as StateFlows. The constructor takes the directory and the file name so tests can isolate it. The default location is a "cases" folder under filesDir.
- Notes, titles and incident dates live on case items, never on evidence rows (D14). The store never calls the vault and has no dependency on it.

STEP 1 (commit "FE-2.5a: CaseStore encrypted file"): create CaseStore.kt (and CaseJson.kt if you want the JSON mapping separate).
a) load(): reads and decrypts the file; if there is no file, the store is empty. If a file exists but cannot be decrypted or parsed, the store enters an UNREADABLE state: it reports a CaseStoreException (reason UNREADABLE), keeps the file UNTOUCHED, and refuses writes. It must NEVER silently reset to empty or overwrite a damaged file.
b) Atomic write: write the new content to a temp file (name ending ".tmp") in the same directory, then replace the real file. EncryptedFile will not overwrite an existing file, so use this sequence: write tmp; rename the current file to ".bak" if it exists; rename tmp to the real name; delete ".bak". On load, if the real file is missing but a ".bak" exists, restore it; if a ".tmp" exists, trust it only if it decrypts and parses (otherwise delete it). A crash at any point must leave at least one readable copy.
c) deleteEverything(): deletes the real file, ".tmp" and ".bak", and empties the in-memory state.
d) No Log.*, println or printStackTrace anywhere. Never print paths, titles, notes or ids.

STEP 2 (commit "FE-2.5b: CaseRepository"): create DefaultCaseRepository.kt implementing the CaseRepository interface on top of CaseStore (every method as specified in the interface KDoc). Rules: ids are random UUID strings; createdAt and addedAt come from the system clock; deleteCase removes its items and never anything else; removeItem never touches evidence; casesForEvidence lists the cases that contain an item with that evidence id; every mutating call persists through the atomic write before it returns; if the store is UNREADABLE every mutating call throws CaseStoreException instead of writing.

STEP 3 (commit "FE-2.5c: case store failure tests"): instrumented tests in app/src/androidTest/java/com/shieldher/dashboard/cases, using an isolated directory (a unique folder name per test class, a canonical-path check, cleanup in teardown) and the Stage 1 isolation pattern.
a) CaseStoreTest: a round trip keeps every field (including isDemo and a null incidentDateMillis); the RAW bytes of the stored file do not contain a recognisable plaintext marker (write a case titled "PLAINTEXT-CHECK-MARKER" and assert the file bytes do not contain that text); a corrupt file (random bytes written over it) leads to UNREADABLE, the file bytes are unchanged afterwards, and a write attempt throws and does NOT reset the file.
b) A simulated mid-write failure: give CaseStore an internal test hook that throws after the tmp file is written and before the replace; assert the previous content is still readable on the next load, and that a leftover ".tmp" does not break loading. Also test recovery from a missing real file with a valid ".bak".
c) DefaultCaseRepositoryTest: create, rename and delete case; add evidence item and note item; update and remove item; casesForEvidence; deleteCase removes its items; removing an item or deleting a case never calls anything outside the store (the repository has no dependency on the vault); deleteEverything removes the file, ".tmp" and ".bak".

STEP 4: VERIFY (no emulator needed)
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests per class and the total.
b) `.\gradlew.bat assembleDebugAndroidTest` must compile.
c) `py -3 scripts/check_tokens.py` must exit 0.
d) Search the new code for Log., println and printStackTrace: there must be none.
e) `git diff main --stat`: only new files under dashboard/cases and its tests; nothing under vault.

STEP 5 (only if I gave a serial): confirm the serial starts with "emulator-", set `$env:ANDROID_SERIAL="<EMULATOR_SERIAL>"`, run `.\gradlew.bat connectedDebugAndroidTest`, and report passed, failed and skipped. If anything fails, copy the test names and the first lines of each error. Do not change any Stage 1 test.

FINISH: three commits as named, then `git push -u origin fe/p2-case-store`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus the instrumented-test results (or "not run: no serial").
```

**You check:**
- Nothing under `vault/` changed.
- The raw-bytes test passes, which proves the case file is not readable text. This also makes the Settings sentence "Case notes are stored encrypted in a separate file" true.
- A corrupt file leads to an error, not an empty store.

---

## P2-4. Wire the real implementations into `ServiceLocator`

**Branch:** `fe/p2-wire-real`
**Commits:** `FE-2.0: VaultEvidenceGateway adapter`, `FE-2.0: ServiceLocator defaults to real implementations`
**Needs merged first:** P2-1, P2-2 and P2-3.
**Why:** until now the screens run on fakes. This makes the real vault and case store the default.

```text
TASK: P2-4 Add the real EvidenceGateway adapter and switch ServiceLocator's defaults from the fakes to the real implementations.

BRANCH: after the start condition passes, run `git switch -c fe/p2-wire-real`.

EXCEPTION TO THE STANDING RULES (this task only): you may create new files under app/src/main/java/com/shieldher/dashboard/data, edit app/src/main/java/com/shieldher/dashboard/data/ServiceLocator.kt, and edit app/src/main/java/com/shieldher/dashboard/MainActivity.kt (only to call ServiceLocator.init). You may NOT edit any vault file.

INPUTS: docs/stage3/vault_api_surface.md; the vault's EvidenceRepository (with the three new methods) and IntegrityResult, EvidenceDisplay, ImportResult; DefaultCaseRepository; the dashboard/data interfaces. STOP if any is missing.

STEP 1 (commit "FE-2.0: VaultEvidenceGateway adapter"): create VaultEvidenceGateway.kt in dashboard/data, implementing EvidenceGateway on top of the vault's EvidenceRepository:
a) observeEvidence() maps the vault's list Flow to EvidenceItem (field for field).
b) getEvidence(id) maps one row or returns null.
c) verify(id) calls verifyIntegrityDetailed and maps IntegrityResult to EvidenceIntegrity (same names).
d) readForDisplay(id, maxBytes) calls the vault's readForDisplay and maps EvidenceDisplay to DisplayContent.
e) importFromUri(uri, mimeType) calls the vault's importFromUri and maps ImportResult to ImportOutcome.
f) delete(id) uses the vault's existing delete-by-id (see section 6 of docs/stage3/vault_api_surface.md); if it does not exist, STOP and tell me (it would be a fourth vault addition).
g) deleteAll() deletes every item through the vault and returns the count. It does NOT touch the case store (Settings delete-all calls the case repository separately, in FE-4.7b).
No Log.*, no printing of ids, paths, hashes or URIs.
Put the pure mapping functions in a separate file (VaultMappers.kt) so they can be unit tested without Android.

STEP 2 (commit "FE-2.0: ServiceLocator defaults to real implementations"):
a) Change ServiceLocator so the defaults are the REAL implementations, created lazily from an application context: add fun init(context: Context) that builds VaultEvidenceGateway(EvidenceRepository(context)), DefaultCaseRepository(...) and keeps the single VerificationSession. Keep installFakes() for tests and the debug gallery. Remove the "TEMPORARY" comment.
b) In MainActivity call ServiceLocator.init(applicationContext) once, before the navigation graph is used. Do not change anything else in MainActivity.
c) Fakes must no longer be reachable from release code paths: keep them in the main source set but mark them internal, and make sure nothing in main code references them except ServiceLocator.installFakes() (used by tests and debug code).
d) Unit tests: VaultMappersTest covering every IntegrityResult, EvidenceDisplay (Text, TooLarge, Unsupported, Unavailable) and ImportResult mapping; these must not need an emulator.

STEP 3: VERIFY
a) `.\gradlew.bat assembleDebug testDebugUnitTest` must pass; report unit tests per class and the total.
b) `py -3 scripts/check_tokens.py` must exit 0.
c) `.\gradlew.bat :app:compileReleaseKotlin` must pass (or fail only for a reason that also fails on main; report it).
d) `git diff main --stat`: only the files named above; nothing under vault.

FINISH: two commits as named, then `git push -u origin fe/p2-wire-real`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules.
```

**You check:** nothing under `vault/` changed, the mapping tests pass, and the release build still compiles. **Then tell Gotam:** the screens now run on the real vault. He can still call `ServiceLocator.installFakes()` for design work.

---

## P2-5. FE-2.4 and FE-2.7: Regression, additivity proof and log audit [AGENT + HUMAN (emulator)]

**Branch:** `fe/p2-verify`
**Commit:** `FE-2.4 FE-2.7: instrumented regression, additivity proof and log audit`
**Needs merged first:** P2-2, P2-3 and P2-4.
**Why:** the claim "Stage 1 is untouched" needs evidence. This produces it, and it is what you show the supervisor and jury.
**Human steps:** start **one emulator** from Android Studio and note the serial.

```text
TASK: P2-5 Prove Stage 1 is intact, record the additivity evidence, and audit logging in all new code. Emulator serial: <EMULATOR_SERIAL> (it must start with "emulator-"). Documentation only: the one file written is docs/stage1_closeout.md.

BRANCH: after the start condition passes, run `git switch -c fe/p2-verify`.

STEP 1: EMULATOR SAFETY CHECK
The serial must start with "emulator-". If it does not, STOP: instrumented tests must never run on a phone. Run `adb devices -l` and confirm the serial is listed and booted.

STEP 2: REGRESSION (run twice)
a) Run `.\gradlew.bat testDebugUnitTest` and report the per-class counts and the total.
b) Set `$env:ANDROID_SERIAL="<EMULATOR_SERIAL>"`. Run `.\gradlew.bat connectedDebugAndroidTest` TWICE, one after the other. Report passed, failed and skipped for each run, and the per-class counts from the second run (app\build\outputs\androidTest-results). Expected: the 14 Stage 1 tests, plus the new vault and case store tests. If anything fails, copy the test names and first lines of each error. Do NOT fix anything.

STEP 3: ADDITIVITY PROOF (read-only)
a) Run `git diff stage1-baseline --numstat -- app/src/main/java/com/shieldher/vault app/src/androidTest app/src/test`. For every file report the added and deleted line counts. The deleted-lines column must be 0 for EVERY Stage 1 file. If it is not, report the file and the lines.
b) List the new files and new methods added to Stage 1 code since the baseline tag (file, method name).
c) Report whether any vault file was changed besides EvidenceRepository.kt, and explain every change.

STEP 4: LOG AUDIT of all NEW code since the baseline
Search the files added or changed since stage1-baseline under app/src/main/java/com/shieldher/vault and app/src/main/java/com/shieldher/dashboard for Log., println, Timber and printStackTrace. List each match as file:line with the line text. For each, say whether it could print content, a file name or path, a hash, a URI, a case title, a note or a typed value (VERIFIED if visible in the line, otherwise INFERENCE). The expected result for new code is NO matches.

STEP 5: RECORD
Append to docs/stage1_closeout.md (keep everything already in it, including earlier sections) a section "## FE-2.4 additivity proof and FE-2.7 log audit (<today's date>)" with the test counts for both runs, the numstat table, the list of added methods, and the log audit table, plus an "Open items" list.

FINISH: `git status --short` must show only docs/stage1_closeout.md modified; `git diff main --stat` must show added lines only. Commit with the message above and run `git push -u origin fe/p2-verify`. Do not open or merge a PR.

REPORT BACK as specified in the standing rules, plus both instrumented-test results, the numstat table and the log audit.
```

**You check:**
- Both instrumented runs pass.
- Every Stage 1 file shows **0 deleted lines**.
- The log audit has no matches in new code.

---

## Phase 2 exit checklist (target Sun 22 Nov)

- [ ] `docs/stage3/vault_api_surface.md` exists and Bhoomika has corrected it.
- [ ] `EvidenceGateway`, `CaseRepository`, `VerificationSession`, the fakes and `ServiceLocator` are on `main`. **Gotam can build screens.**
- [ ] The three vault methods exist, with **0 deleted lines** in every Stage 1 file.
- [ ] The case store is encrypted, and the raw-bytes test passes. A corrupt file gives an error, never an empty store.
- [ ] `ServiceLocator` defaults to the real implementations, and the fakes are unreachable from release code paths.
- [ ] All 14 Stage 1 instrumented tests plus the new ones pass **twice on an emulator**.
- [ ] No logging of content, paths, hashes, URIs, titles or notes in new code.
- [ ] Every Phase 2 pull request was reviewed by someone other than its author.

## Add to the freeze checklist (3 Dec)

- [ ] `ServiceLocator` uses the **real** implementations (no fake is installed in the shipped flow).
- [ ] The Settings sentence about encrypted case notes stays only if the raw-bytes test passes.

## Things to know before you start

- **The proposal sentence about case notes.** Wording Block 4 says case notes are "stored encrypted in a separate file". This is true only once P2-3's raw-bytes test passes.
- **Backup.** The case file sits in the app's private storage. Whether it is excluded from Android backup is the open FE-0.3 item. Don't write any sentence about backup until that is confirmed.
- **Fourth addition risk.** If P2-0 shows there is no delete-by-id or no decrypt-to-memory API, that is a fourth vault addition. Tell me and I'll adjust, because it changes Bhoomika's workload and needs the supervisor's approval under "existing code untouched".

## After Phase 2

**Phase 4 (the screens)** runs from 20 Nov to 1 Dec, with the core flow due **Fri 27 Nov**. It builds against `ServiceLocator`. I can write the Phase 4 prompts next. They should start immediately with the fake-backed screens, since P2-1 unblocks them.