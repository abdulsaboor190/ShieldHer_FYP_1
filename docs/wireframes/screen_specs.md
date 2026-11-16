# Screen Specifications for Wireframes
FE-1.2 Support Document — Written for Gotam to construct Figma wireframes.

---

## 1. Global Rules & Guidelines

- **Resolution / Frame size:** Mobile width (390dp x 844dp or 412dp x 915dp standard phone viewport).
- **Typography:** Android System Font (Roboto). Body never below 14sp.
- **Palette & Tints:** Strictly adhere to `docs/design_basis.md`. No raw amber or grey fills for text/icons. Use badge tint tokens with their respective text-version colours.
- **Tone:** Calm, plain, supportive, non-judgemental. Short sentences (`docs/wording_guide.md`).
- **No Reliance on Colour Alone:** Every status badge or indicator must combine **icon + text label + colour**.

---

## 2. Navigation Architecture

### Bottom Navigation Bar (4 Destinations)
1. **Home:** Vault overview, recent items, primary actions.
2. **Evidence:** Searchable evidence list, intake button.
3. **Cases:** Organised case folders and chronological timelines.
4. **More:** Secondary destinations (Settings & Privacy, Account Check, Help & Resources).

### Navigation Hierarchy Map
```
[Bottom Nav]
 ├── 1. Home
 ├── 2. Evidence ──> 3. Evidence Detail ──> Add to Case Dialog / Delete Confirm Dialog
 │                 └──> 4. Import Evidence (System Picker)
 ├── 3. Cases ──> 5. Cases List ──> Create Case Dialog
 │             └──> 6. Case Timeline ──> Add Evidence Picker / Add Note Dialog
 └── 4. More
       ├── 7. Settings & Privacy ──> Delete All Confirm Dialog / "What ShieldHer cannot prove"
       ├── 8. Demo Mode (Debug builds only, banner persistent on all screens)
       ├── 9. Onboarding (First launch or re-opened from Settings)
       ├── 10. Account Check (Layout-only preview)
       └── 11. Help & Resources (Moved to FYP-II unless verified)
```

---

## 3. Shared Components Spec

| Component | Visual & Behavioural Definition | Used By |
|---|---|---|
| **StatusBadge** | Tint background (e.g. #E6F2EE, #F8E8E9, #ECEDEF) + text-version icon (16dp) + bold text label (12–14sp). Accessible content description matching text. | Evidence List, Evidence Detail, Case Timeline |
| **EvidenceCard** | White card surface (#FFFFFF) on off-white screen (#F8FAF7) with 1dp outline (#75817D / #A8D5C8) or elevation 1dp, thumbnail/preview icon, title/source, and timestamp. | Home, Evidence List, Case Timeline |
| **SectionHeader** | 16sp bold deep teal (#164E50) header text with optional subtitle or action link. | Home, Detail, Case Timeline, Settings |
| **EmptyState** | Centred vertical stack: 48dp Material vector icon + 16sp heading + 14sp explanatory body + primary action button. Never uses illustrative cartoons. | Home, Evidence List, Cases List, Case Timeline |
| **ErrorState** | Calm notification banner or full-page state: red tint background (#F8E8E9) + error icon + plain non-alarmist explanation. | Detail, Import, Cases |
| **ConfirmDialog** | Modal dialog with bold title, explanatory body, outlined "Cancel" button, and deep teal or solid red destructive button (#AD464E with white text). | Detail (Delete), Cases (Delete Case), Settings (Delete All) |

---

## 4. Screen-by-Screen Specifications

---

### Screen 1: Home
- **Purpose:** Gives the user an immediate calm overview of their saved evidence, case counts, and quick actions upon opening the app.
- **Arrival & Actions:**
  - Arrive via: App launch (after onboarding) or Bottom Navigation "Home" tab.
  - Actions:
    - Tap "Import Evidence" button -> Opens Import Evidence (Screen 4).
    - Tap "View All Evidence" -> Switches to Evidence List (Screen 2).
    - Tap an item in "Recent Evidence" -> Opens Evidence Detail (Screen 3).
    - Tap a case in "Active Cases" -> Opens Case Timeline (Screen 6).
- **Screen Elements:**
  - Top app bar: "ShieldHer" app title (no neon branding).
  - Quick summary cards: Total evidence count; active cases count.
  - Primary button: "Import evidence" (Deep teal #164E50).
  - SectionHeader: "Recent evidence" with "See all" text button.
  - Recent list: 3 most recent EvidenceCards.
  - SectionHeader: "Active cases".
- **Data Source:** Room DB (`EvidenceDao`), Case Store.
- **States:**
  - *Content:* Shows summary numbers, recent cards, active case cards.
  - *Empty (Zero Evidence):* EmptyState with `ic_status_not_checked` icon, text: `"No evidence saved to this phone yet"` (PROPOSED: needs Abdul's review), button `"Import evidence"`.
  - *Loading:* Ocean teal spinner (#267878).
- **Exact Strings:**
  - `"Saved to this phone"`
  - `"No evidence saved to this phone yet"` (PROPOSED: needs Abdul's review)
  - `"Import evidence"` (PROPOSED: needs Abdul's review)

---

### Screen 2: Evidence List
- **Purpose:** Lets the user browse, filter, and search through all evidence items saved in the vault.
- **Arrival & Actions:**
  - Arrive via: Bottom Navigation "Evidence" tab or "See all" from Home.
  - Actions:
    - Type in search field -> Filters list by source label or receipt date.
    - Tap date filter chip -> Filters by date range.
    - Tap EvidenceCard -> Opens Evidence Detail (Screen 3).
    - Tap Floating Action Button (FAB) -> Opens Import Evidence (Screen 4).
- **Screen Elements:**
  - Top search bar with search icon and clear button.
  - Filter chips row (Source, Date).
  - Vertical list of EvidenceCards:
    - Thumbnail or MIME-type vector icon.
    - Source label (e.g., "In-app import", "WhatsApp share").
    - "Received by ShieldHer" date/time.
    - StatusBadge: `"Not checked yet"` (grey) or cached session result (`"Unchanged since saved"`, `"Changed or unreadable since saved"`, `"File missing"`).
  - Extended FAB: `"Import evidence"`.
- **Data Source:** Room DB (`EvidenceRepository.getAllEvidence()`) and in-memory `VerificationSession`.
- **States:**
  - *Default/Content:* Populated list of EvidenceCards with status badges.
  - *Search Filter Active:* Filtered items matching query.
  - *No Search Results:* EmptyState: `"No matches found"` (PROPOSED: needs Abdul's review), subtitle `"Try searching by another date or source label"` (PROPOSED: needs Abdul's review).
  - *Empty Vault:* EmptyState: `"No evidence saved yet"` (PROPOSED: needs Abdul's review).
- **Exact Strings:**
  - `"Received by ShieldHer"`
  - `"Not checked yet"`
  - `"Unchanged since saved"`
  - `"Changed or unreadable since saved"`
  - `"File missing"`
  - `"No matches found"` (PROPOSED: needs Abdul's review)

---

### Screen 3: Evidence Detail
- **Purpose:** Displays full cryptographic metadata, stored encrypted size, in-memory preview, verification action, linked cases, and deletion option for a single evidence item.
- **Arrival & Actions:**
  - Arrive via: Tapping an EvidenceCard in Home, Evidence List, or Case Timeline.
  - Actions:
    - Tap "Copy" icon next to SHA-256 -> Copies full hash string to clipboard with toast: `"Hash copied to clipboard"` (PROPOSED: needs Abdul's review).
    - Tap "Verify" button -> Executes cryptographic hash check against stored file bytes; updates StatusBadge.
    - Tap "Add to case" -> Opens case selector dialog.
    - Tap "Delete" button -> Opens ConfirmDialog (Screen 3-Delete).
    - Tap "Back" arrow -> Returns to previous screen.
- **Screen Elements:**
  - Top app bar with back navigation and item source label title.
  - In-memory preview container: Image view (downscaled in memory) or text scroll box, or generic file icon if preview unavailable.
  - Status section: Current StatusBadge (prominently displayed) + "Verify" button (ocean teal).
  - Explanatory block (under badge when verified): Exact Block 1 text.
  - Metadata table:
    - **SHA-256 Hash:** Full 64-character hex string + copy button.
    - **Received by ShieldHer:** Formatted date, time, and phone time zone (e.g., "16 Nov 2026, 09:30 PKT").
    - **Source label:** e.g., "In-app import".
    - **MIME type:** e.g., "image/png", "text/plain".
    - **Stored size:** File size formatted in KB/MB, explicitly labelled as `"Encrypted size on disk"` (PROPOSED: needs Abdul's review).
  - Linked cases section: List of case tags; "Add to case" button.
  - Destructive action: "Delete evidence" text button (#AD464E).
- **Data Source:** Vault Room DB row + `EncryptedFileStore.readForDisplay()` + `VerificationSession`.
- **States:**
  - *Initial (Not checked yet):* Badge displays `"Not checked yet"` (grey `ic_status_not_checked`). "Verify" button enabled.
  - *Verifying (Loading):* Ocean teal spinner on "Verify" button.
  - *Verified: Unchanged:* StatusBadge `"Unchanged since saved"` (green `ic_status_unchanged`). Block 1 explanatory text displayed below.
  - *Verified: Changed or unreadable:* StatusBadge `"Changed or unreadable since saved"` (red `ic_status_changed` warning icon). Explanatory text: `"The file on disk does not match the cryptographic hash recorded upon receipt."` (PROPOSED: needs Abdul's review).
  - *Verified: File missing:* StatusBadge `"File missing"` (red `ic_status_missing`). Explanatory text: `"The encrypted file could not be found on storage."` (PROPOSED: needs Abdul's review).
  - *Preview unavailable:* Clean grey box with document icon and label: `"Preview unavailable for this file format"` (PROPOSED: needs Abdul's review).
  - *Delete Confirm Dialog:* Modal dialog with Block 3 text:
    `"This removes the file and its record from this phone. It cannot be undone. Case notes that mention it stay, marked 'evidence deleted'."`
    Buttons: `"Cancel"` and solid red `"Delete"`.
- **Exact Strings:**
  - Block 1: `"The saved copy is identical to what ShieldHer received. It does not show who made it, when it was originally made, or whether it was edited before you shared it. It does not decide whether it can be used in a legal process."`
  - Block 2: `"'Received by ShieldHer' comes from this phone's clock. 'You entered' is a date you typed yourself."`
  - Block 3: `"This removes the file and its record from this phone. It cannot be undone. Case notes that mention it stay, marked 'evidence deleted'."`
  - `"Received by ShieldHer"`
  - `"Not checked yet"`
  - `"Unchanged since saved"`
  - `"Changed or unreadable since saved"`
  - `"File missing"`
  - `"Encrypted size on disk"` (PROPOSED: needs Abdul's review)
  - `"Hash copied to clipboard"` (PROPOSED: needs Abdul's review)

---

### Screen 4: Import Evidence
- **Purpose:** Guides the user to select an evidence file via the Android system document picker, validating size and encrypting it directly into the vault.
- **Arrival & Actions:**
  - Arrive via: "Import evidence" FAB or button on Home or Evidence List.
  - Actions:
    - Tap "Select file" -> Launches Android `ActivityResultContracts.GetContent` system document picker.
    - File selected -> Triggers `importFromUri(uri, mime)`; streams into `EncryptedFileStore`.
    - Success -> Navigates directly to Evidence Detail (Screen 3) for the imported item with a confirmation toast.
- **Screen Elements:**
  - Top bar with back arrow and title `"Import evidence"`.
  - Supportive instructional text: `"Select an image, screenshot, audio recording, or document to store in the encrypted vault."` (PROPOSED: needs Abdul's review).
  - Source label field (default: `"In-app import"`).
  - Large drop/selection card with import icon and `"Choose file from phone"` button.
  - Notice text: `"Files up to 500 MB are encrypted immediately upon selection."` (PROPOSED: needs Abdul's review).
- **Data Source:** System URI -> `EvidenceRepository.importFromUri()`.
- **States:**
  - *Ready:* Selection button enabled.
  - *Importing / Encrypting:* Ocean teal progress bar + text `"Encrypting and hashing evidence..."` (PROPOSED: needs Abdul's review).
  - *Error: Too Large:* Red banner: `"File exceeds the 500 MB limit. Please select a smaller file."` (PROPOSED: needs Abdul's review).
  - *Error: Read Failure:* Red banner: `"Could not read the selected file. Please ensure the file is accessible."` (PROPOSED: needs Abdul's review).
- **Exact Strings:**
  - `"Saved to this phone"`
  - `"Choose file from phone"` (PROPOSED: needs Abdul's review)
  - `"Encrypting and hashing evidence..."` (PROPOSED: needs Abdul's review)

---

### Screen 5: Cases List & Create
- **Purpose:** Displays all user-created cases (folders) that group related evidence items, with an action to create a new case.
- **Arrival & Actions:**
  - Arrive via: Bottom Navigation "Cases" tab.
  - Actions:
    - Tap "Create case" button / FAB -> Opens "Create Case" modal dialog.
    - Tap Case Card -> Opens Case Timeline (Screen 6).
    - Long-press / Overflow menu on Case Card -> Options: "Rename case", "Delete case".
- **Screen Elements:**
  - Top app bar: `"Cases"`.
  - Extended FAB / Button: `"New case"`.
  - List of Case Cards:
    - Case title.
    - Item count (e.g., "4 items").
    - Last updated date ("You entered" or recent item timestamp).
- **Data Source:** Encrypted `CaseStore`.
- **States:**
  - *Content:* Populated list of cases.
  - *Empty:* EmptyState with folder icon, text: `"No cases created yet"` (PROPOSED: needs Abdul's review), subtitle: `"Group related evidence items into cases to build an organized timeline."` (PROPOSED: needs Abdul's review).
  - *Create Case Dialog:* Text input for case title, buttons: `"Cancel"`, `"Create"`.
  - *Delete Case Dialog:* ConfirmDialog with text: `"Delete this case? Evidence files will remain safely stored in your vault."` (PROPOSED: needs Abdul's review), buttons: `"Cancel"`, `"Delete"`.
- **Exact Strings:**
  - `"You entered"`
  - `"No cases created yet"` (PROPOSED: needs Abdul's review)
  - `"Delete this case? Evidence files will remain safely stored in your vault."` (PROPOSED: needs Abdul's review)

---

### Screen 6: Case Timeline
- **Purpose:** Presents a chronological timeline of evidence items and notes belonging to a specific case, ordered by incident date.
- **Arrival & Actions:**
  - Arrive via: Tapping a case on Cases List (Screen 5) or Home.
  - Actions:
    - Tap "Add evidence" -> Opens evidence picker dialog (lists vault items not yet in case).
    - Tap "Add note" -> Opens "Add Note" dialog with date picker ("You entered" date).
    - Tap an Evidence item -> Opens Evidence Detail (Screen 3).
    - Tap "Remove from case" -> Removes link from case (evidence stays in vault).
- **Screen Elements:**
  - Top app bar: Case name, back navigation, overflow menu (Rename, Delete).
  - Floating actions or header action bar: `"Add evidence"`, `"Add note"`.
  - Timeline list: Vertical timeline track with timeline nodes.
    - Each node shows date header:
      - If user entered incident date: `"You entered: 14 Oct 2026"`.
      - Fallback: `"Received by ShieldHer: 16 Nov 2026, 09:30"`.
    - Node content card: Evidence thumbnail + title + StatusBadge, OR note text box.
    - Action icon: "Remove from case".
- **Data Source:** Encrypted `CaseStore` cross-referenced with `EvidenceDao`.
- **States:**
  - *Timeline with Items:* Chronologically sorted by "You entered" date (falling back to "Received by ShieldHer").
  - *Evidence Deleted State:* If the underlying evidence file was deleted from vault, node remains in timeline showing grey StatusBadge `"Evidence deleted"`, with text: `"This evidence item was removed from the phone vault."` (PROPOSED: needs Abdul's review).
  - *Empty Timeline:* EmptyState: `"This case has no items yet. Add evidence or notes to begin your timeline."` (PROPOSED: needs Abdul's review).
  - *Add Note Dialog:* Input fields: Note text; optional incident date picker ("You entered"); buttons: `"Cancel"`, `"Save"`.
- **Exact Strings:**
  - Block 2: `"'Received by ShieldHer' comes from this phone's clock. 'You entered' is a date you typed yourself."`
  - `"Received by ShieldHer"`
  - `"You entered"`
  - `"Evidence deleted"`
  - `"This case has no items yet. Add evidence or notes to begin your timeline."` (PROPOSED: needs Abdul's review)

---

### Screen 7: Settings & Privacy
- **Purpose:** Provides user control over screenshot protection, technical storage disclosures, bulk data wiping, and app transparency.
- **Arrival & Actions:**
  - Arrive via: Bottom Navigation "More" tab -> "Settings & Privacy".
  - Actions:
    - Toggle "Screenshot protection" -> Enables/disables `FLAG_SECURE`.
    - Tap "What ShieldHer cannot prove" -> Opens informational disclosure sub-page.
    - Tap "Delete all evidence and cases" -> Opens irreversible ConfirmDialog.
- **Screen Elements:**
  - Top app bar: `"Settings & Privacy"`.
  - Section 1: **Security**
    - Item: `"Screenshot protection"`. Switch toggle (default: ON). Subtext: `"Blocks screenshots and screen recording across all vault screens."` (PROPOSED: needs Abdul's review).
    - *(Note: NO app-lock row or biometric toggle per decision D15).*
  - Section 2: **Vault Storage**
    - Item: Summary counter (e.g., `"12 items saved to this phone"`).
    - Information Card displaying **exact Block 4 text** regarding encryption keys and unencrypted metadata DB.
    - Link item: `"What ShieldHer cannot prove"` -> opens explanatory page containing Block 1.
  - Section 3: **Danger Zone**
    - Destructive button: `"Delete all evidence and data"` (#AD464E outline or text).
- **Data Source:** SharedPreferences (`FLAG_SECURE`), Room DB counts.
- **States:**
  - *Standard View:* Switches reflect user preferences.
  - *Delete All Confirm Dialog:* Full modal warning with red header: `"Delete all evidence and records?"` (PROPOSED: needs Abdul's review), body: `"This will permanently erase all encrypted evidence files, technical records, cases, and notes from this phone. This action cannot be undone."` (PROPOSED: needs Abdul's review), buttons: `"Cancel"` and solid red `"Delete everything"`.
- **Exact Strings:**
  - Block 4: `"Evidence files are stored encrypted, with keys held in the Android Keystore. Case notes are stored encrypted in a separate file. The vault's technical record of each item (receipt hash, receipt time, time zone, source label, file size and file path) is kept in the app's private storage in a database that is not encrypted."`
  - `"Saved to this phone"`
  - `"Screenshot protection"` (PROPOSED: needs Abdul's review)
  - `"What ShieldHer cannot prove"` (PROPOSED: needs Abdul's review)
  - `"Delete all evidence and records?"` (PROPOSED: needs Abdul's review)

---

### Screen 8: Demo Mode (Debug Builds Only)
- **Purpose:** Provides jury presentation functionality by seeding synthetic evidence and cases, displaying a persistent watermark, and disabling screenshot blocking.
- **Arrival & Actions:**
  - Arrive via: Automatic in debug builds via Settings or debug menu trigger.
  - Actions:
    - Tap "Reset demo data" -> Restores default 6–8 synthetic items and 2 demo cases.
    - Tap "Exit demo mode" -> Clears demo seed and restores standard state.
- **Screen Elements:**
  - **Persistent Banner:** A prominent top banner displayed across all screens:
    Amber/grey tint container with bold text: `"Demo data — Not real evidence"` (PROPOSED: needs Abdul's review).
  - Floating or embedded debug pill: `"Reset Demo"` button.
- **Data Source:** Hardcoded synthetic seeder (`isDemo = true`).
- **States:**
  - *Active:* `FLAG_SECURE` disabled so projectors/screenshots work; "Demo data" banner pinned.
  - *Resetting:* Quick spinner -> UI refreshes with seed dataset.
- **Exact Strings:**
  - `"Demo data — Not real evidence"` (PROPOSED: needs Abdul's review)
  - `"Reset demo data"` (PROPOSED: needs Abdul's review)

---

### Screen 9: Onboarding (Max 3 Pages)
- **Purpose:** Onboards first-time users with essential privacy and evidence custody expectations in 3 concise, supportive steps.
- **Arrival & Actions:**
  - Arrive via: App launch on fresh install, or manually re-opened from Settings.
  - Actions:
    - Swipe or tap "Next" -> Advances page 1 -> 2 -> 3.
    - Tap "Get Started" on page 3 -> Sets `onboarding_complete` flag and opens Home.
- **Screen Elements:**
  - Progress dots (3 dots) using ocean teal (#267878).
  - Page 1:
    - Icon: Shield with check vector.
    - Title: `"Private Evidence Vault"` (PROPOSED: needs Abdul's review).
    - Body: `"Securely store photos, screenshots, and messages on this phone with tamper-evident cryptographic verification."` (PROPOSED: needs Abdul's review).
  - Page 2:
    - Icon: Clock and calendar vector.
    - Title: `"Trusted Timelines"` (PROPOSED: needs Abdul's review).
    - Body: Uses Block 2 explanation: `"'Received by ShieldHer' comes from this phone's clock. 'You entered' is a date you typed yourself."`
  - Page 3:
    - Icon: Key vector.
    - Title: `"Your Privacy & Ownership"` (PROPOSED: needs Abdul's review).
    - Body: `"No accounts, no cloud sync, no tracking. Your evidence never leaves your device unless you choose to share it."` (PROPOSED: needs Abdul's review).
  - Button: `"Next"` on pages 1–2; `"Get Started"` on page 3.
- **Data Source:** Static assets + SharedPreferences.
- **States:**
  - Page 1, Page 2, Page 3.
- **Exact Strings:**
  - Block 2: `"'Received by ShieldHer' comes from this phone's clock. 'You entered' is a date you typed yourself."`
  - `"Get Started"` (PROPOSED: needs Abdul's review)

---

### Screen 10: Account Check (Layout-Only Preview)
- **Purpose:** Demonstrates the planned interface for the harassment risk screening tool without executing network models or presenting synthetic risk scores.
- **Arrival & Actions:**
  - Arrive via: Bottom Navigation "More" tab -> "Account Check".
  - Actions:
    - Select platform (Instagram, Twitter/X).
    - Fill synthetic input fields (handle, followers, following, bio text, link yes/no).
    - Tap "Check Account" button -> Triggers informational dialog or inline banner displaying only: `"Coming in the next version"`.
    - No network call is made; nothing typed is stored in any database.
- **Screen Elements:**
  - Top app bar: `"Account Check"`.
  - Prominent grey preview badge at top:
    StatusBadge with `ic_status_preview` and exact text: `"Preview: not working yet"`.
  - Platform selector dropdown / chips: `"Instagram"`, `"X / Twitter"`.
  - Input field 1: `"Account handle or username"` (PROPOSED: needs Abdul's review).
  - Input field 2: `"Followers count"` (numeric) (PROPOSED: needs Abdul's review).
  - Input field 3: `"Following count"` (numeric) (PROPOSED: needs Abdul's review).
  - Input field 4: `"Description / bio text"` (multiline) (PROPOSED: needs Abdul's review).
  - Input toggle: `"External link in bio (Yes / No)"` (PROPOSED: needs Abdul's review).
  - Primary button: `"Check account"` (PROPOSED: needs Abdul's review).
  - Informational card: `"Coming in the next version. Real machine-learning evaluation models will be connected in FYP-II."` (PROPOSED: needs Abdul's review).
  - **CRITICAL RESTRICTION:** NO probability numbers, NO risk percentages, NO result colours (green/amber/red), NO words "fake" or "bot".
- **Data Source:** In-memory transient UI state only (never persisted).
- **States:**
  - *Default Input Form:* Shows fields with grey "Preview: not working yet" badge.
  - *Button Tapped:* Shows banner / dialog: `"Coming in the next version"`.
- **Exact Strings:**
  - `"Preview: not working yet"`
  - `"Coming in the next version"`
  - `"Account handle or username"` (PROPOSED: needs Abdul's review)
  - `"Check account"` (PROPOSED: needs Abdul's review)

---

## 5. Wireframe Construction Checklist for Gotam

When creating the wireframes in Figma:
- [ ] Every frame is drawn at phone viewport width (390dp to 412dp).
- [ ] Every state described above is represented in at least one artboard.
- [ ] Every status display includes **icon + text label + tint colour** (never colour alone).
- [ ] Tint backgrounds match the exact hex tokens from `docs/design_basis.md`:
  - Success: #E6F2EE (text #2F6F5A)
  - Amber: #FBF1E0 (text #8F5E12)
  - Grey: #ECEDEF (text #666A72)
  - Red: #F8E8E9 (text #AD464E)
- [ ] No Compose elements; all designs map directly to Android Views + Material 3 components.
- [ ] Wording strictly adheres to `docs/wording_guide.md` (no forbidden words like "Verified", "Tampered", "Fake", "Bot", or "Protected").
