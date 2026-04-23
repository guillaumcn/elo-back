---
name: Group Join Request gap analysis
description: Gap analysis for the SubmitJoinRequest / ListJoinRequests / HandleJoinRequest feature reviewed on 2026-04-23
type: project
---

Reviewed 2026-04-23. Feature: `group_join_request.feature`. Started with 11 scenarios, added 11 new ones (total 22). All 110 Cucumber tests pass after additions.

**Gaps found and fixed:**

Critical:
- G1: 3 missing 401 unauthenticated scenarios (one per new endpoint: submit, list, handle)
- G2: 404 when submitting to non-existent group
- G3: 404 when handling non-existent join request
- G4: 400 when PUT body missing required `status` field (`@NotNull` on HandleJoinRequestRequest.status)
- G5: 404 when listing requests for non-existent group

Important:
- G6: 422 on deny→deny (only approve→approve was tested; both statuses trigger ensureNotAlreadyResolved)
- G7: 409 when group admin submits request to own group (already a member by creation)
- G8: `resolvedBy` and `resolvedAt` asserted in approval response
- G9: empty list assertion for admin with no pending requests

**New steps added to GroupSteps.java:**
- `When I submit a join request for the group without authentication`
- `When I list join requests for the group without authentication`
- `When I handle a join request without authentication`
- `And a non-existent join request id`
- `When I handle the join request with no status`
- `And I have denied the join request`
- `And the join request resolver is set`
- `And the join requests list is empty`

**Recurring pattern confirmed:** Authorization (401) gaps and 404-for-non-existent-resource gaps are the two most reliable gap categories across all Group bounded context features. Every new endpoint set has been missing both.

**CucumberHooks status:** `GroupJoinRequestJpaRepository.deleteAll()` was already present in `CucumberHooks.java` — no hook gap.

**Why:** The recurring gap pattern is now well-established: each new endpoint cluster is initially authored without the boilerplate 401/404 scenarios. Flag these first in every future review.

**How to apply:** In any future Group (or other bounded context) review, check 401 and 404 coverage before anything else. Expect them to be missing. Also check 400 on any endpoint that uses `@Valid @RequestBody` — the annotation validation path is consistently undertested.
