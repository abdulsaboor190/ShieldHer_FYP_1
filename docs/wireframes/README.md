# ShieldHer Wireframes (FE-1.2)

**Deliverable:** FE-1.2 (Wireframes for screens 1 to 7)  
**Date:** 17 November 2026 (Design Freeze)  
**Author / Designer:** Gotam  
**Reference Specification:** [docs/wireframes/screen_specs.md](screen_specs.md)  
**Design Basis & Palette:** [docs/design_basis.md](../design_basis.md)  
**Wording & Copy Guide:** [docs/wording_guide.md](../wording_guide.md)  

---

## Screen Inventory

The Figma project files for Screens 1 through 7 are archived locally under [`docs/wireframes/figma/`](figma/):

| File | Screen Number | Screen Name | Description & States Covered |
|---|---|---|---|
| `figma/1.fig` | Screen 1 | **Home** | Dashboard overview, encrypted item count, integrity summary, quick actions, recent evidence list. |
| `figma/2.fig` | Screen 2 | **Evidence List** | Searchable evidence vault, source/date filtering, list cards, empty state, and intake trigger. |
| `figma/3.fig` | Screen 3 | **Evidence Detail** | Full SHA-256 hash, verified/tampered/missing status badges, in-memory preview, case linking, delete confirmation. |
| `figma/4.fig` | Screen 4 | **Import Evidence** | File picker integration, optional metadata (incident date, case, note), limit validation. |
| `figma/5.fig` | Screen 5 | **Cases List** | Case overview folders, active case count, create case modal. |
| `figma/6.fig` | Screen 6 | **Case Timeline** | Chronological timeline, dual timestamp tracking ("Received by ShieldHer" vs "You entered"), note cards, deleted item placeholder. |
| `figma/7.fig` | Screen 7 | **Settings & Privacy** | Screenshot protection toggle (FLAG_SECURE), storage breakdown, legal disclaimers, delete all reset action. |

---

## Design Checklist Verification
- [x] Phone width dimensions (390 x 844 px / 412 x 915 px).
- [x] Every status indicator pairs an **icon + label text + tint color** (no color-only reliance).
- [x] Uses design tokens from `docs/design_basis.md` (Teal `#164E50`, Forest Green `#2D6A4F`, Off-white `#F8FAF7`).
- [x] Uses approved strings from `docs/wording_guide.md`.
