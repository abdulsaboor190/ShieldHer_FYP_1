## What this PR does
Deliverable ID (for example FE-4.3a):
Summary:

## How I tested it
- [ ] `.\gradlew.bat assembleDebug testDebugUnitTest` passes
- [ ] Tested on emulator / phone (say which):
- [ ] Screenshots attached (for UI changes)

## Standing rules checklist
- [ ] Stage 1 vault files: added lines only, nothing changed or deleted
- [ ] The UI never calls a DAO or EncryptedFileStore directly
- [ ] No network code
- [ ] No logging of content, file paths, hashes, notes, case titles or typed values
- [ ] Nothing decrypted is written to cache, temp files or external storage
- [ ] All user-facing text is in strings.xml and follows docs/wording_guide.md
- [ ] No hard-coded colours or sizes in layouts
- [ ] Every screen has empty, loading, error and success states
- [ ] Debug-only tools sit behind BuildConfig.DEBUG
- [ ] No dependency or version change without team agreement (toolchain is frozen until 10 Dec)
- [ ] After the 3 Dec freeze: fixes only

## Reviewer
Requested reviewer:
(Review within the same day. If you cannot, say so on the PR so someone else can.)