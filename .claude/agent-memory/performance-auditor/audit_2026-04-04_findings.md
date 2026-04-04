---
name: Performance audit findings 2026-04-04
description: Key performance issues found in initial full-codebase audit -- N+1 queries, missing transactions, missing indexes, double JWT parsing
type: project
---

Full performance audit completed 2026-04-04 covering Identity and Group bounded contexts (Activity, Match, Ranking contexts not yet implemented).

Critical findings:
- N+1 query pattern in GroupRepositoryAdapter.findAllByMemberId -- countByGroupId called per group in a loop
- N+1 on every findById/save in GroupRepositoryAdapter due to memberCount sub-query
- Zero @Transactional annotations in the entire codebase -- multi-step writes (CreateGroupUseCase: save group + save member) have no atomicity guarantee
- Missing index on group_members.user_id -- full table scan on findGroupIdsByUserId
- Double JWT parsing in JwtAuthenticationFilter (validateToken + getUserIdFromToken both parse the token)

**Why:** These are confirmed code-level issues, not hypothetical. The N+1 and missing index compound: list-groups endpoint produces O(N) queries each hitting an unindexed scan.

**How to apply:** When reviewing new code in Group, Activity, Match, or Ranking contexts, check for the same patterns: (1) loop-driven count queries in repository adapters, (2) missing @Transactional on multi-step use cases, (3) missing DB indexes for new query patterns in JPA repositories.
