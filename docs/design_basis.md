# Design Basis
FE-1.3: colour system and component rules

## 1. Direction

Modern, calm, trustworthy, warm, not clinical. Do not make the whole UI green: balance white surfaces, pale mint areas and deep teal. No neon, no heavy gradients. Never rely on colour alone: every status uses icon plus text plus colour.

## 2. Core Palette

| Role | Name | Hex | Use |
|---|---|---|---|
| Primary | Deep teal | #164E50 | Headers, primary buttons, selected navigation |
| Secondary | Ocean teal | #267878 | Highlights, links, loading indicators, selected states |
| Accent | Soft turquoise | #55A6A0 | **DECORATION ONLY:** never text, never icons, never loading or selected states, because it fails 3:1 on white (2.86), off-white (2.72), pale mint (2.44) |
| Supporting | Warm mint | #A8D5C8 | Sparingly; only main text #263635 or deep teal #164E50 on it |
| Light surface | Pale mint | #E4F0E9 | Cards, secondary surfaces, containers |
| Background | Off-white | #F8FAF7 | Main screen background |
| Card | White | #FFFFFF | Elevated cards, dialog backgrounds |
| Text | Main text | #263635 | Primary content text |
| Text | Secondary text | #5A6662 | Subtitles, timestamps, captions (darkened from #75817D, which was 4.0 on white) |

## 3. Semantic Colours

| Meaning | Fill (tinted background only) | Text and icon colour |
|---|---|---|
| Success | Green fill #38836B | Text/icon #2F6F5A |
| Uncertain | Amber fill #D69A42 | Text/icon #8F5E12 |
| Neutral / insufficient | Grey fill #898D95 | Text/icon #666A72 |
| Error / destructive | Red fill #C45B62 | Text/icon #AD464E |

**Rule:** The fill colours are never used for icons or text and never as a solid background behind white text; icons and text use the text/icon colour.

## 4. Badge Tint Tokens (FE-1.3c)

- **Success:** #E6F2EE with text #2F6F5A
- **Amber:** #FBF1E0 with text #8F5E12
- **Grey:** #ECEDEF with text #666A72
- **Red:** #F8E8E9 with text #AD464E
- **Destructive button:** Solid #AD464E with white text

## 5. State-to-Colour and Icon Mapping (FE-1.3b)

| State | Colour | Planned Icon File Name | Notes |
|---|---|---|---|
| Unchanged since saved | Green | `ic_status_unchanged` | Exists (Material check_circle) |
| Changed or unreadable since saved | Red | `ic_status_changed` | Warning icon, distinct from missing |
| File missing | Red | `ic_status_missing` | Distinct icon |
| Not checked yet | Grey | `ic_status_not_checked` | Default state before verification |
| Evidence deleted | Grey | `ic_status_deleted` | Tombstone state for deleted items |
| Preview: not working yet (Account Check) | Grey | `ic_status_preview` | Layout-only preview badge |

*Note: Green is used only on badges and icons, never on buttons or headers. Account Check shows no result colours in FYP-I.*

## 6. Material 3 Role Mapping

Fixed Light theme, dynamic colour off (`DS-3`):

- **primary:** #164E50
- **onPrimary:** #FFFFFF
- **primaryContainer:** #E4F0E9
- **onPrimaryContainer:** #164E50
- **secondary:** #267878
- **onSecondary:** #FFFFFF
- **secondaryContainer:** #A8D5C8
- **onSecondaryContainer:** #164E50
- **tertiary:** #55A6A0
- **onTertiary:** #263635
- **tertiaryContainer:** #A8D5C8
- **onTertiaryContainer:** #164E50
- **background:** #F8FAF7
- **onBackground:** #263635
- **surface:** #FFFFFF
- **onSurface:** #263635
- **surfaceVariant:** #E4F0E9
- **onSurfaceVariant:** #5A6662
- **outline:** #75817D (boundaries only, 3:1)
- **outlineVariant:** #A8D5C8
- **surfaceContainerLowest:** #FFFFFF
- **surfaceContainerLow:** #F8FAF7
- **surfaceContainer:** #F8FAF7
- **surfaceContainerHigh:** #E4F0E9
- **surfaceContainerHighest:** #E4F0E9
- **inverseSurface:** #263635
- **inverseOnSurface:** #F8FAF7
- **inversePrimary:** #A8D5C8
- **error:** #AD464E
- **onError:** #FFFFFF
- **errorContainer:** #F8E8E9
- **onErrorContainer:** #AD464E
- **scrim:** #000000

*Rule: Never call DynamicColors; force light mode (DS-3); the component gallery (FE-3.5) must show every component on white, off-white and pale mint to catch default-colour leaks.*

## 7. Layout Tokens

Proposed defaults (confirm at FE-3.1c):
- **Spacing:** 4 / 8 / 12 / 16 / 24 / 32 dp
- **Typography:** Five text styles with body never below 14 sp
- **Shapes:** One card radius and one button radius
- **Touch targets:** At least 48 dp
- **System font only:** No custom font downloads
- **Hygiene:** No hard-coded colours, sizes or user-facing strings in layouts
- **Empty state:** Uses a Material vector icon (no illustrations)

## 8. Components

- **StatusBadge:** Tint background, text-version icon and text, content description equal to the text
- **EvidenceCard:** White on off-white background
- **SectionHeader:** Clear section grouping
- **EmptyState:** Clean layout with vector icon
- **ErrorState:** Non-alarmist, actionable error representation
- **ConfirmDialog:** Delete confirmation uses wording block 3 in `docs/wording_guide.md`
- **Loading indicator:** Ocean teal (#267878)
- **Primary button:** Deep teal (#164E50) with white text
- **Destructive button:** Solid #AD464E with white text

## 9. Contrast Rules

- Text at least 4.5:1
- Icons and UI boundaries at least 3:1
- Verified by `docs/contrast_check.py` (run it after any colour change) and by Accessibility Scanner on real screens

## Superseded

`docs/reference/design_v1.md` is superseded where it says amber or grey fills can be used for icons or that soft turquoise can be used for loading or selected states.

## Icons

Icons are Material vector icons, licence Apache 2.0. Added so far: ic_status_unchanged (Material check_circle), ic_status_changed (Material warning), ic_status_missing (Material cancel), ic_status_not_checked (Material schedule), ic_status_deleted (Material delete), ic_status_preview (Material visibility), ic_nav_home (Material home), ic_nav_evidence (Material folder), ic_nav_cases (Material assignment), ic_nav_more (Material more_horiz).
