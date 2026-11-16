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
| D3 | Account Check is layout only in FYP-I; no result shown; real results appear only in the jury slides | Team | Locked |
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
