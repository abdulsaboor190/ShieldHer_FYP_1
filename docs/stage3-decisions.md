# Stage 3 Decisions
FE-0.1: decisions D1 to D17 and DS-1 to DS-4

## Design tooling

- Wireframe tool: Figma (free)
- DS-4 Icon set: Material vector icons (Apache License 2.0), added as vector drawables. Icon names follow the pattern ic_status_<state>, for example ic_status_unchanged.
- Font: system font only; no custom or downloaded font (fewer licensing and loading problems).


## Decisions (recorded 16 Nov 2026)

| ID | Decision | Owner | Status |
|---|---|---|---|
| D1 | Views + Material 3, not Compose | Gotam | Locked |
| D2 | English only; all text in strings.xml | Team | Locked |
| D3 | Account Check is layout only in FYP-I; no result shown; real results appear only in the jury slides | Team | Superseded by D18 |
| D4 | Feature freeze Thu 3 Dec 2026; submission Thu 10 Dec 2026 | Team | Locked |
| D5 | Vault changes: three additive methods only (verifyIntegrityDetailed, readForDisplay, importFromUri); existing vault code untouched; no database migration | Bhoomika | Locked |
| D6 | Cases live in a separate small store; vault untouched | Bhoomika + Gotam | Locked |
| D7 | No SQLCipher; the proposal sentence is changed to say evidence files are encrypted and the vault metadata database is not | Bhoomika | Locked |
| D8 | Screenshot protection (FLAG_SECURE) on by default with a Settings toggle; demo mode turns it off with a banner | Team | Locked |
| D9 | Two kinds of time: "Received by ShieldHer" (phone clock, never editable) and "You entered" (optional incident date), always labelled | Team | Locked |
| D10 | Integrity is checked only when the user taps Verify; results are kept in memory for the session | Bhoomika | Locked |
| D11 | Demo mode: fake evidence and two fake cases, bannered "Demo data", with reset | Gotam | Locked |
| D12 | Case store is an encrypted file (Jetpack Security EncryptedFile), written atomically | Bhoomika + Gotam | Accepted (16 Nov) |
| D13 | New screens never call sweepOrphans() (option b), pending the FE-0.6 findings below | Bhoomika | Accepted pending FE-0.6 |
| D14 | Notes and incident dates live on case items, not on evidence rows | Team | Accepted (16 Nov) |
| D15 | No app lock in FYP-I; no lock row in Settings | Team | Accepted (16 Nov) |
| D16 | Tamper-demo tool: yes, debug builds only, first to cut | Abdul + Bhoomika | Accepted (16 Nov) |
| D17 | Help screen moves to FYP-II unless Abdul has verified every contact from official pages by 25 Nov | Abdul | Accepted (16 Nov) |
| D18 | Account Check runs the frozen model on the phone (reverses D3) | Team | Accepted (25 Nov) |
| DS-1 | "Changed or unreadable since saved" uses muted red with a warning icon, a different icon from "File missing"; amber is reserved for "uncertain" (Account Check, FYP-II) | Team | Accepted (16 Nov) |
| DS-2 | Badges use a light tinted background with text-version colour for text and icon | Gotam | Accepted (16 Nov) |
| DS-3 | Light theme only, forced; never call DynamicColors | Gotam | Accepted (16 Nov) |
| DS-4 | Material vector icons, Apache 2.0 (already recorded under Design tooling) | Gotam | Locked |

## Screen list (frozen 16 Nov 2026)

1. Home (Must)
2. Evidence list (Must)
3. Evidence detail (Must)
4. Import evidence (Must)
5. Cases list and create (Must)
6. Case timeline (Must)
7. Settings and privacy (Must)
8. Demo mode, debug builds only (Must for the jury demo)
9. Onboarding, max 3 pages (Should)
10. Account Check, layout only (Should)
11. Help and resources (moved to FYP-II unless verified by 25 Nov, see D17)

**Cut order if time runs out:** 11, 10, 9, tamper tool, polish of 7.
**Never cut:** screens 2 to 6, the Verify button, demo mode, Settings delete-all and the limits page.


## FE-0.6 findings (sweepOrphans investigation)

### 1. Call Sites in `app/src`
- `app/src/main/java/com/shieldher/vault/data/EvidenceRepository.kt:57` — Definition of `suspend fun sweepOrphans(): Int`.
- `app/src/main/java/com/shieldher/vault/intake/IntakeActivity.kt:113` — In `handleIncomingIntent() -> lifecycleScope.launch -> withContext(Dispatchers.IO)`. Invoked immediately prior to `repo.intake(...)`.
- `app/src/main/java/com/shieldher/vault/ui/EvidenceListActivity.kt:54` — In `onCreate() -> lifecycleScope.launch`. Invoked on activity start before collecting evidence flow.
- `app/src/androidTest/java/com/shieldher/vault/OrphanSweepTest.kt:120` — In instrumented unit test `sweepOrphans_deletesOnlyUnbackedNonInFlightFile()`.

### 2. In-Flight Tracking Mechanism
- **Declaration:** Declared in `EvidenceRepository.Companion` (`EvidenceRepository.kt:36`):
  `private val inFlightIds = ConcurrentHashMap.newKeySet<String>()`
- **Scope & Thread Safety:** Because it is declared on the `companion object`, it is shared across all instances of `EvidenceRepository` within the same JVM process. `ConcurrentHashMap.newKeySet()` is thread-safe.

### 3. Repository Instantiation
- `IntakeActivity.kt:111`: Instantiates `EvidenceRepository(this@IntakeActivity)` per intake intent.
- `EvidenceListActivity.kt:24`: Instantiates `lazy { EvidenceRepository(this) }`.
- `EvidenceListViewModel.kt:11`: Instantiates `EvidenceRepository(application)`.
- Each call site constructs its own distinct instance of `EvidenceRepository` (it is not a singleton class, though its underlying `EvidenceDatabase` is a Room singleton).

### 4. Code vs Inference Answers
- **(1) Can a sweep started from one repository instance delete a file that another instance is still writing?**
  - *Verified in code:* Within the same process, no. `inFlightIds` is held in the static `companion object` shared by all repository instances. During `intake()`, the UUID is inserted into `inFlightIds` before file creation and removed only in the `finally` block after DB insertion. `sweepOrphans()` checks `inFlightIds.contains(fileName)` and skips matching files.
  - *Inference / Edge cases:* If intake crashed or the OS killed the process during write, the file remains on disk with no DB record; upon next launch, `inFlightIds` is clean and `sweepOrphans()` deletes the partial file. Furthermore, if any background service or multi-process component were introduced, companion state would not bridge processes.
- **(2) Which Stage 1 screen triggers the sweep, and what happens when EvidenceListActivity is replaced by a fragment?**
  - *Verified in code:* Stage 1 triggers the sweep in `EvidenceListActivity.onCreate()` and `IntakeActivity.handleIncomingIntent()`.
  - *Inference:* When `EvidenceListActivity` is replaced by `EvidenceListFragment` hosted inside `MainActivity`, `EvidenceListActivity` is no longer in the backstack. Unless the new fragment explicitly launches `sweepOrphans()`, the list-screen sweep ceases to run. `IntakeActivity` will continue to run its pre-intake sweep.

### 5. Recommendation for D13
- **Decision:** Keep **option (b)** ("New screens never call `sweepOrphans()`").
- **Rationale:** Triggering filesystem sweeps during fragment navigation or UI rendering causes unnecessary background disk I/O and latency on app start. `IntakeActivity` already cleans up before accepting new files. Option (c) (`sweepStaleOrphans()`) is unnecessary overhead for FYP-I and remains deferred.

---

## D18 Account Check runs the frozen model on the phone (reverses D3)

- **Context & Reversal:** Decision D3 initially limited Account Check to a static preview layout for FYP-I. Because the Stage 2 Random Forest model (300 trees, 5 features) was successfully frozen with full determinism, it has been exported into a compact binary format (`account_risk_model_v1.bin`, 918,820 bytes) and evaluated directly on-device in pure Kotlin. D3 is therefore reversed and superseded by D18.
- **On-Device Evaluation & Privacy:**
  - Evaluates entirely on the phone CPU without network calls.
  - Zero permissions added: `android.permission.INTERNET` remains strictly absent from the Android Manifest.
  - No typed data (followers, following, bio) is stored in SharedPreferences, SQLite, Room, or disk cache. Typed bio is converted to a Unicode code point count and discarded immediately.
  - The `AccountCheckViewModel` contains zero `String` fields, and `onDestroyView` clears all inputs and transient state.
- **Model Integrity & Golden Set Parity:**
  - The model binary was exported read-only from the frozen Stage 2 bundle (`ml` commit `c014a1264a5648a48ad59713910ff1c694606f87`).
  - Binary SHA-256: `ad11c13741920a37702b8b2e82fabf147015e6e0f29b249bf979cf09228ccc55` (matches `docs/stage3/account_risk_model_v1.manifest.json`).
  - Evaluator parity verified across all 1,090 golden test rows (600 boundary, 350 random, 90 edge, 50 unsupported):
    - Raw probability max difference: $9.992 \times 10^{-16} \le 1.0 \times 10^{-9}$ (PASS).
    - Calibrated probability max difference: $9.992 \times 10^{-16} \le 1.0 \times 10^{-9}$ (PASS).
    - Label and band agreement: 1,090 / 1,090 (100% exact match).
  - The Stage 2 `ml/` repository directory was completely untouched (freeze verified before and after).
- **Scope & Presentation Rules:**
  - **Instagram Only:** Supported platforms constant is restricted to Instagram. Other platforms (e.g. X/Twitter, Other) immediately display "Not validated for this platform".
  - **Bands, Never Numbers:** The UI presents only categorical qualitative bands ("Higher resemblance", "Lower resemblance", "Not enough confidence to say"), never a probability, percentage, or numeric risk score.
  - **Colour Rules:** Amber token (`@color/sh_risk_amber`) is used solely for "Higher resemblance". Grey token (`@color/sh_risk_neutral`) is used for all other states. Green and red tokens are strictly forbidden to avoid false assurance or accusation.
  - **No Explanations / TreeSHAP:** Feature importance and TreeSHAP are excluded because feature stability under distribution shift was not tested for mobile delivery.
- **Timeline & Gate Status:**
  - Implementation & verification completed 25 Nov 2026.
  - All go/no-go gates passed with zero regressions.

---

## Freeze checklist additions
1. Asset `app/src/main/assets/account_risk_model_v1.bin` SHA-256 matches `docs/stage3/account_risk_model_v1.manifest.json`.
2. Export verification script `python -B scripts/export_risk_model.py --check` reports `OK`.
3. Merged debug and release manifests have zero `INTERNET` permission.
4. Account Check presents only categorical risk bands and never displays probabilities or percent signs.
5. Supported platforms constant is strictly Instagram only (`RiskPlatform.INSTAGRAM`).

