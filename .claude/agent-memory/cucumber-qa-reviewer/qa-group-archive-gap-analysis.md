---
name: Group bounded context QA gap analysis (archive/unarchive + CRUD deep review)
description: Detailed gap analysis for all Group feature files after archive/unarchive use cases were added (2026-04-04)
type: project
---

# Group Bounded Context — QA Gap Analysis

**As of:** 2026-04-04, after `ArchiveGroupUseCase` and `UnarchiveGroupUseCase` were added as untracked files.

## Scope

Feature files reviewed:
- `src/test/resources/features/group/group_crud.feature` (19 scenarios)
- `src/test/resources/features/group/group_archive.feature` (7 scenarios)

Step definitions: `src/test/java/com/elo/acceptance/group/GroupSteps.java`

## Key architecture notes

- `UpdateGroupUseCase`: non-member → 404 (info-hiding), non-admin member → 403. Does NOT block on archived group.
- `ArchiveGroupUseCase` / `UnarchiveGroupUseCase`: same 404/403 split as update. Non-member → 404, non-admin member → 403.
- `GetGroupUseCase`: non-member → 404, member (any role) → 200.
- `CreateGroupUseCase`: creator auto-added as ADMIN member with memberCount=1 after create.
- `GroupResponse` fields: id, name, description, joinPolicy, archived, createdBy, createdAt, memberCount.
- `JoinPolicy` enum values: OPEN, INVITATION, REQUEST.
- Description max length: 1000 chars (DTO @Size + domain validateDescription).
- Name max length: 100 chars (DTO @Size + domain validateName).

## CucumberHooks status

GroupJpaRepository and GroupMemberJpaRepository both registered — no data isolation risk.

## Critical gaps (7 identified)

- C1: No 401 guard on PUT /groups/{id}. New step needed: `When I submit the update group request without authentication`
- C2: No 401 guard on POST /groups/{id}/archive. New step needed: `When I archive the group without authentication`
- C3: No 401 guard on POST /groups/{id}/unarchive. New step needed: `When I unarchive the group without authentication`
- C4: Non-member archive → 404 not tested (only non-admin member → 403 is covered)
- C5: Non-member unarchive → 404 not tested (only non-admin member → 403 is covered)
- C6a: Archive non-existent group → 404 not tested
- C6b: Unarchive non-existent group → 404 not tested
- C7: Update non-existent group → 404 not tested (non-member update exists but that group exists)

## Important gaps (8 identified)

- I1: List groups must include groups where user is plain MEMBER (not admin) — not tested
- I2: GroupResponse.id is never asserted as non-null UUID in any scenario
- I3: Create group description > 1000 chars → 400 not tested
- I4: Update group name > 100 chars → 400 not tested
- I5: Update group description > 1000 chars → 400 not tested
- I6: Update an archived group — behavior undocumented (currently succeeds, no 422 guard in UpdateGroupUseCase)
- I7: Create group with invalid JoinPolicy enum string → 400 not tested
- I8: Create group with name field absent entirely → 400 not tested (differs from blank "")

## Nice-to-have gaps (4 identified)

- N1: Update combining name + joinPolicy in one request
- N2: GroupResponse id is a valid non-null UUID (assertion on create response)
- N3: Newly created group has archived=false (assertion step exists: `the group is not archived`)
- N4: List groups does not include groups from other users — isolation check. New step needed: `the group list does not contain {string}`

## New step definitions needed (6)

All additive — no existing step modifications required:
1. `When I submit the update group request without authentication` — PUT unauthenticated
2. `When I archive the group without authentication` — POST archive unauthenticated
3. `When I unarchive the group without authentication` — POST unarchive unauthenticated
4. `When I submit the create group request omitting the name` — POST with Map body lacking name key
5. `And the group list does not contain {string}` — noneMatch assertion on list response
6. Fix: `the create group join policy is {string}` parses enum eagerly — must be deferred for invalid-value test (I7)

## No CommonSteps conflicts

All proposed steps are distinct from CommonSteps.java patterns.

**How to apply:** When implementing these scenarios, check this file first to avoid duplicating steps already listed here. When Activity/Match/Ranking contexts are added, check whether the same 401/non-member/invalid-field gap pattern recurs.
