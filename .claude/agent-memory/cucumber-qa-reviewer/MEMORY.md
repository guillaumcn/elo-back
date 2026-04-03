---
name: cucumber-qa-reviewer memory index
description: Index of QA analysis artifacts for the ELO Ranking App Cucumber acceptance tests
type: project
---

# Cucumber QA Reviewer — Memory Index

## Files

| File | Description |
|---|---|
| `qa-gap-analysis.md` | Full QA gap analysis report covering Identity and Group bounded contexts. Contains scenario inventory, coverage checklist, gap classification (critical / important / nice-to-have), complete copy-paste-ready Gherkin for 31 new scenarios, step definition requirements, and conflict warnings. |

## Key Findings (summary)

- **29 existing scenarios** across registration, login, profile, and group CRUD.
- **5 critical gaps**: missing 401 guards on Group endpoints, missing BR8 test on `PUT /groups/{id}` for non-members, missing unknown `groupId` → 404, missing username length boundary on registration, missing login validation (400) paths.
- **8 important gaps**: thin response body assertions (no id/email checks), missing partial profile update paths, missing group description assertions, missing tampered JWT test, missing public profile email-exclusion assertion.
- **6 nice-to-have gaps**: username case sensitivity, idempotent same-username update, list ordering, archived field default, count changes, deleted user public profile.
- **31 new scenarios** proposed with full Gherkin, all following the project step pattern.
- **~15 new step definitions** needed, all additive (no existing steps modified). Key conflict warning: `pendingUpdate*` field initialization in `ProfileSteps` must be refactored to support bio-only and avatar-only partial update steps without side effects.

## Scope

Bounded contexts: **Identity** and **Group** (as of 2026-04-04, commit `fd925de`).
Not yet covered: Activity, Match, Ranking contexts (not yet implemented).
