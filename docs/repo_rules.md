# ShieldHer Repository Rules

## a) Branch Naming
- Branch naming convention: `fe/<area>-<short-name>` (for example `fe/e7-gradle-flags`, `fe/p4-evidence-list`).
- Exactly one deliverable per branch.

## b) Pull Requests
- Every change to `main` must go through a pull request with at least one approval from another team member.
- The PR template (`.github/pull_request_template.md`) must be filled in completely.
- Keep PRs small and focused on a single deliverable.
- The requested reviewer must respond within the same day (or explicitly state so on the PR so another reviewer can be assigned).
- The author does not merge their own PR.

## c) Commits
- Commit messages must start with the deliverable ID (for example `FE-4.2a: evidence list with search`).

## d) Freeze
- Hard freeze date: No new features after Thursday, 3 December 2026.
- After 3 December: Fixes, wording changes, tests, and presentation work only.

## e) Manual GitHub Settings Checklist
Checklist for repository owner (to be configured manually in GitHub web settings):
- [ ] Repository pushed to GitHub
- [ ] All three members added as collaborators
- [ ] Branch protection enabled on `main`:
  - [ ] Require a pull request before merging
  - [ ] Require at least 1 approval
  - [ ] Block force pushes
  - [ ] Dismiss stale approvals when new commits are pushed
- [ ] Default branch is set to `main`

## f) Stage 1 Isolation
- The `app/src/main/java/com/shieldher/vault/` folder is add-only.
- Never edit or delete existing vault lines or files.