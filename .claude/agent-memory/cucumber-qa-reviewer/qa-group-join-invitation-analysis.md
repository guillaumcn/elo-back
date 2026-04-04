---
name: Group Join & Invitation gap analysis
description: Coverage audit of group_join.feature against the three new use cases (JoinGroup, CreateGroupInvitation, JoinGroupByInvitation) added in commit after ce35d59
type: project
---

Audit performed 2026-04-04 after new files: JoinGroupUseCase, CreateGroupInvitationUseCase, JoinGroupByInvitationUseCase, GroupAlreadyMemberException, GroupInvitationExpiredException, GroupInvitationNotFoundException.

**Critical finding — missing step definition:** The feature file uses the step
`And another user has created a group named {string} with join policy {string}`
but no matching `@And` annotation exists in GroupSteps.java. This causes an
undefined step failure for 2 scenarios ("Join an open group" and "Cannot directly
join a non-open group"). A new step must be added.

**Coverage status after review:**
- group_join.feature has 12 scenarios covering most happy paths and error paths.
- Missing auth scenarios for join, create-invitation, and join-by-invitation (401 unauthenticated).
- Missing: create invitation on non-existent group (404).
- Missing: join-by-invitation on an archived group (422).
- Missing: invitation with past expiry date assertion on response.
- Missing: join-by-invitation response field assertions (role, groupId).
- Missing: REQUEST join policy cannot use direct join (behaviour equivalent to INVITATION).
- Missing: create invitation for a group with OPEN join policy (should succeed — any member can invite).

**Why:** These gaps mean the authorization boundary and several business-rule combinations for the new membership flow go untested at the acceptance level.

**How to apply:** When generating Gherkin for this context, prioritize the missing step definition fix first, then the unauthenticated 401 scenarios for join/invitation endpoints, then the OPEN-policy invitation scenario, then remaining business-rule edge cases.
