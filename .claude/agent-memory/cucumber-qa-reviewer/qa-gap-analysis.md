---
name: cucumber-qa-gap-analysis
description: Full QA gap analysis for Identity and Group Cucumber acceptance tests — identifies missing scenarios, writes complete Gherkin, flags new step definitions needed
type: project
---

# QA Report — ELO Ranking App (Identity + Group Contexts)

_Generated: 2026-04-04_

---

## 1. Existing Scenario Inventory

### Identity — Registration (6 scenarios)

| # | Scenario | HTTP | Assertion |
|---|---|---|---|
| R1 | Successful registration | 201 | JWT + user profile |
| R2 | Duplicate username | 409 | Error message |
| R3 | Duplicate email | 409 | Error message |
| R4 | Invalid email format | 400 | Status only |
| R5 | Blank username | 400 | Status only |
| R6 | Password too short (< 8 chars) | 400 | Status only |

### Identity — Login (6 scenarios)

| # | Scenario | HTTP | Assertion |
|---|---|---|---|
| L1 | Successful login | 200 | JWT |
| L2 | Wrong password | 401 | Status only |
| L3 | Non-existent email | 401 | Status only |
| L4 | Deleted account login | 401 | Status only |
| L5 | Access protected endpoint without token | 401 | Status only |
| L6 | Access protected endpoint with expired token | 401 | Status only |

### Identity — Profile (7 scenarios)

| # | Scenario | HTTP | Assertion |
|---|---|---|---|
| P1 | Get own profile | 200 | Username |
| P2 | Update own profile (username + bio) | 200 | Username |
| P3 | Update username to taken value | 409 | Status only |
| P4 | View another user's public profile | 200 | Username |
| P5 | View non-existent user's profile | 404 | Status only |
| P6 | Delete own account | 204 | Can no longer login |
| P7 | Deleted account is anonymized | 204 | DB data anonymized |

### Group — CRUD (10 scenarios)

| # | Scenario | HTTP | Assertion |
|---|---|---|---|
| G1 | Create group (name + join policy) | 201 | Name, policy, memberCount=1, creator |
| G2 | Create group with description | 201 | Name |
| G3 | Create group without join policy | 400 | Status only |
| G4 | Get group details as member | 200 | Name, memberCount |
| G5 | Get group details as non-member | 404 | Status only |
| G6 | Update group name as admin | 200 | New name |
| G7 | Update group join policy as admin | 200 | New policy |
| G8 | Update group as non-admin member | 403 | Status only |
| G9 | List my groups (two groups) | 200 | Both names present |
| G10 | List groups when member of none | 200 | Empty list |

**Total: 29 existing scenarios**

---

## 2. Coverage Checklist

### Identity — Registration

| Rule / Path | Covered |
|---|---|
| Happy path — returns 201, JWT, user profile | YES (R1) |
| Duplicate username → 409 | YES (R2) |
| Duplicate email → 409 | YES (R3) |
| Invalid email format → 400 | YES (R4) |
| Blank username → 400 | YES (R5) |
| Password too short → 400 | YES (R6) |
| Username too short (< 3 chars) → 400 | NO |
| Username too long (> 50 chars) → 400 | NO |
| Blank password → 400 | NO |
| Missing required fields (null) → 400 | NO |
| Response body contains user id, email, createdAt | NO |

### Identity — Login

| Rule / Path | Covered |
|---|---|
| Happy path — returns 200, JWT, user profile | YES (L1) |
| Wrong password → 401 | YES (L2) |
| Non-existent email → 401 | YES (L3) |
| Deleted account → 401 | YES (L4) |
| No token on protected endpoint → 401 | YES (L5) |
| Expired token on protected endpoint → 401 | YES (L6) |
| Invalid email format in login request → 400 | NO |
| Blank email → 400 | NO |
| Blank password → 400 | NO |
| Login response also contains user profile | NO |
| Tampered/invalid JWT signature → 401 | NO |

### Identity — Profile

| Rule / Path | Covered |
|---|---|
| Get own profile → 200 with username | YES (P1) |
| Get own profile — response includes email, id, createdAt | NO |
| Get own profile without auth → 401 | NO |
| Update username (happy path) → 200 | YES (P2) |
| Update bio only (no username change) → 200 | NO |
| Update avatarUrl | NO |
| Update username to same value (no-op) → 200 | NO |
| Update username too short → 400 | NO |
| Update username too long → 400 | NO |
| Update username to taken → 409 | YES (P3) |
| Update avatarUrl too long → 400 | NO |
| Update profile without auth → 401 | NO |
| View another user's public profile → 200 | YES (P4) |
| Public profile does NOT expose email | NO |
| View non-existent user → 404 | YES (P5) |
| View public profile without auth → 401 | NO |
| Delete own account → 204 | YES (P6) |
| Deleted account data anonymized (DB) | YES (P7) |
| Delete account without auth → 401 | NO |
| Update profile after own deletion → 401 | NO |

### Group — CRUD

| Rule / Path | Covered |
|---|---|
| Create group happy path — 201, name, policy, memberCount, createdBy | YES (G1) |
| Create group with description | YES (G2) |
| Creator is admin (BR2) | Partial (G1 asserts createdBy, not role) |
| Missing join policy → 400 | YES (G3) |
| Blank group name → 400 | NO |
| Group name > 100 chars → 400 | NO |
| Create group without auth → 401 | NO |
| Get group as member → 200 | YES (G4) |
| Get group as non-member → 404 (BR8, not 403) | YES (G5) |
| Get group without auth → 401 | NO |
| Get group with unknown groupId → 404 | NO |
| Update group name as admin → 200 | YES (G6) |
| Update join policy as admin → 200 | YES (G7) |
| Update description as admin | NO |
| Update group as non-admin member → 403 | YES (G8) |
| Update group as non-member → 404 (BR8) | NO |
| Update group without auth → 401 | NO |
| Update group: blank name → 400 | NO |
| Update group: name > 100 chars → 400 | NO |
| List my groups — multiple groups | YES (G9) |
| List my groups — empty | YES (G10) |
| List my groups without auth → 401 | NO |
| User can belong to multiple groups (BR6) | Partial (G9 via created groups, not joined) |

---

## 3. Gap Analysis

### Critical Gaps

These gaps represent missing auth guards, security boundary violations, or core business rule paths that must not be absent from a production test suite.

#### CG-1: Missing 401 guards on all authenticated endpoints

No scenario verifies that unauthenticated requests to protected endpoints return 401 for the Group endpoints or for non-auth identity endpoints (update profile, delete account, view public profile). The current LoginSteps covers only `/users/me` GET — all other endpoints have zero auth guard coverage.

Affected endpoints: `GET /users/me` (partially covered), `PUT /users/me`, `DELETE /users/me`, `GET /users/{username}`, `POST /groups`, `GET /groups`, `GET /groups/{id}`, `PUT /groups/{id}`.

Priority: critical because a misconfigured `SecurityFilterChain` (e.g. a rule added with wrong matcher order) would go undetected.

#### CG-2: Update group as non-member returns 404 (BR8), not tested

BR8 states non-members receive 404 to avoid leaking group existence. G5 covers `GET /groups/{id}` for non-members. But `PUT /groups/{id}` for a completely unrelated user (not even a member) is untested — `UpdateGroupUseCase` handles this path and throws `GroupNotFoundException` when the user is neither admin nor member.

#### CG-3: Get group with completely unknown groupId returns 404

G5 tests the "non-member" case but an entirely non-existent `groupId` is a distinct code path (GroupRepositoryPort returns empty → GroupNotFoundException). Currently untested.

#### CG-4: Registration validation boundary cases — username length

The domain enforces `USERNAME_MIN_LENGTH = 3`. A 2-character username is a boundary violation. R5 covers blank, but the 2-char case tests the actual numeric constraint. Similarly, 51-char username is untested.

#### CG-5: Login request validation (400 paths)

`LoginRequest` has `@NotBlank` and `@Email` on `email`, and `@NotBlank` on `password`. No scenario submits a blank email, blank password, or malformed email to `/auth/login`. These return 400, not 401 — distinct handling that needs verification.

---

### Important Gaps

#### IG-1: Response body assertions are thin

- Registration (R1): asserts JWT and `username` but not `id`, `email`, or `createdAt` fields.
- Login (L1): asserts JWT but not the embedded user profile.
- Profile (P1): asserts `username` only, not `id`, `email`, `bio`, `avatarUrl`, `createdAt`.
- Public profile (P4): asserts `username` only; critically, email must NOT appear in the public profile (PublicUserResponse excludes it — this needs a negative assertion).
- Group (G1): asserts name, policy, memberCount, createdBy — reasonably complete, but `id` and `description` are not checked.

#### IG-2: Update profile — partial update paths not tested

`UpdateProfileRequest` treats all fields as optional. Three untested paths:
- Update `bio` only (no username change) — exercises `isUsernameChanging()` returning false.
- Update `avatarUrl` — field has its own `@Size(max=500)` validation.
- Updating username to the same current value should succeed with 200 (the use case short-circuits the uniqueness check).

#### IG-3: Group creation response — description field

G2 tests creating with a description but never asserts the description is present in the response body. A mapper bug that drops the field would go undetected.

#### IG-4: Update group description

`UpdateGroupRequest` allows updating `description`. No scenario exercises this. The domain `Group.update()` handles it, but it is completely untested at the acceptance level.

#### IG-5: Creator is admin (BR2) — role assertion

G1 verifies `createdBy` (the user ID) but never verifies that the creator has the ADMIN role. The only way this matters end-to-end is when another scenario (update group, eventually add-member operations) behaves differently based on role — but BR2 itself has no direct assertion.

#### IG-6: Tampered JWT returns 401

A JWT with a valid structure but wrong signature is a realistic attack. The expired-token scenario (L6) tests expiry but not signature tampering. `LoginSteps.buildExpiredToken()` produces a correctly signed token — a test with an arbitrarily different key is missing.

#### IG-7: Update profile — validation errors

- Username 2 chars → 400
- AvatarUrl > 500 chars → 400
These exercise the `@Size` annotations on `UpdateProfileRequest`.

#### IG-8: Group name validation on create/update

- Blank name on create → 400 (domain throws `InvalidGroupException`)
- Name > 100 chars on create → 400
- Name > 100 chars on update → 400
G3 covers missing `joinPolicy` but not name constraints.

---

### Nice-to-Have Gaps

#### NTH-1: Registration — case sensitivity of username/email

Is "Alice" the same as "alice"? The current implementation uses plain string equality. Clarifying behavior with a scenario documents the decision.

#### NTH-2: Idempotency of profile update with same username

Submitting the same username that the user already has should return 200. The `isUsernameChanging()` guard skips the uniqueness check. A scenario makes this explicit and guards against future regressions.

#### NTH-3: List groups — ordering

`GET /groups` returns all groups the user belongs to. No scenario asserts order. If the spec eventually mandates alphabetical or creation-time order, a missing scenario will be the first signal of a regression.

#### NTH-4: Group response — archived field defaults to false

`GroupResponse` includes `archived`. No scenario asserts it defaults to `false` on creation.

#### NTH-5: Multiple admins / group membership count changes

These are not yet surfaced by existing endpoints (no join/leave/add-member APIs exist yet), but the `memberCount` is asserted as 1 after creation. Once membership APIs arrive, count assertions will need to be updated and extended here.

#### NTH-6: Profile — deleted user's public profile returns 404

`GetPublicProfileUseCase.findActiveUserByUsername()` filters out deleted users. A scenario that attempts to view the public profile of a deleted user confirms this behaviour. P5 uses a non-existent username; this uses a real but deleted one.

---

## 4. Suggested Gherkin Scenarios (complete, copy-paste-ready)

### File: `features/identity/registration.feature` — additions

```gherkin
  Scenario: Registration with username that is too short
    Given a registration request with username "ab"
    And the registration email is "alice@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration with username that is too long
    Given a registration request with username "this_username_is_way_too_long_and_exceeds_fifty_chars"
    And the registration email is "alice@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration with blank password
    Given a registration request with username "alice"
    And the registration email is "alice@example.com"
    And the registration password is ""
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration response contains expected user fields
    Given no user exists with email "carol@example.com" or username "carol"
    And a registration request with username "carol"
    And the registration email is "carol@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 201 Created response
    And the response contains a valid JWT token
    And the response contains the user profile with username "carol"
    And the user profile contains an id
    And the user profile email is "carol@example.com"
```

### File: `features/identity/login.feature` — additions

```gherkin
  Scenario: Login with invalid email format
    Given a login request with email "not-an-email"
    And the login password is "Str0ngP@ss!"
    When I submit the login request
    Then I receive a 400 Bad Request response

  Scenario: Login with blank email
    Given a login request with email ""
    And the login password is "Str0ngP@ss!"
    When I submit the login request
    Then I receive a 400 Bad Request response

  Scenario: Login with blank password
    Given a login request with email "alice@example.com"
    And the login password is ""
    When I submit the login request
    Then I receive a 400 Bad Request response

  Scenario: Login response contains user profile
    Given a registered user with email "alice@example.com" and password "Str0ngP@ss!"
    And a login request with email "alice@example.com"
    And the login password is "Str0ngP@ss!"
    When I submit the login request
    Then I receive a 200 OK response
    And the response contains a valid JWT token
    And the login response contains the user profile

  Scenario: Access protected endpoint with tampered token
    When I access a protected endpoint with a tampered token
    Then I receive a 401 Unauthorized response
```

### File: `features/identity/profile.feature` — additions

```gherkin
  Scenario: Get own profile without authentication
    When I access a protected endpoint without a token
    Then I receive a 401 Unauthorized response

  Scenario: Get own profile returns all expected fields
    Given I am authenticated as "alice"
    When I request my profile
    Then I receive a 200 OK response
    And the profile username is "alice"
    And the profile contains an id
    And the profile email is "alice@example.com"

  Scenario: Update bio only without changing username
    Given I am authenticated as "alice"
    And a profile update request with bio "I love table tennis"
    When I submit the profile update
    Then I receive a 200 OK response
    And the profile bio is "I love table tennis"

  Scenario: Update avatar URL
    Given I am authenticated as "alice"
    And a profile update request with avatar url "https://example.com/avatar.png"
    When I submit the profile update
    Then I receive a 200 OK response
    And the profile avatar url is "https://example.com/avatar.png"

  Scenario: Update username to same current value succeeds
    Given I am authenticated as "alice"
    And a profile update request with username "alice"
    When I submit the profile update
    Then I receive a 200 OK response
    And the profile username is "alice"

  Scenario: Update username that is too short
    Given I am authenticated as "alice"
    And a profile update request with username "ab"
    When I submit the profile update
    Then I receive a 400 Bad Request response

  Scenario: Update avatar URL that is too long
    Given I am authenticated as "alice"
    And a profile update request with avatar url "https://example.com/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    When I submit the profile update
    Then I receive a 400 Bad Request response

  Scenario: Update profile without authentication
    When I access a protected endpoint without a token
    Then I receive a 401 Unauthorized response

  Scenario: Public profile does not expose email
    Given a user exists with username "bob"
    And I am authenticated as "alice"
    When I request the public profile of "bob"
    Then I receive a 200 OK response
    And the public profile username is "bob"
    And the public profile does not contain an email

  Scenario: View public profile without authentication
    When I access a protected endpoint without a token
    Then I receive a 401 Unauthorized response

  Scenario: Delete account without authentication
    When I access a protected endpoint without a token
    Then I receive a 401 Unauthorized response

  Scenario: Deleted user's public profile returns not found
    Given a user with username "ghost" has deleted their account
    And I am authenticated as "alice"
    When I request the public profile of "ghost"
    Then I receive a 404 Not Found response
```

### File: `features/group/group_crud.feature` — additions

```gherkin
  Scenario: Create group without authentication
    And a create group request with name "Ping Pong Club"
    And the create group join policy is "OPEN"
    When I submit the create group request
    Then I receive a 401 Unauthorized response

  Scenario: Create group with blank name
    Given I am authenticated as "alice"
    And a create group request with name ""
    And the create group join policy is "OPEN"
    When I submit the create group request
    Then I receive a 400 Bad Request response

  Scenario: Create group with name exceeding 100 characters
    Given I am authenticated as "alice"
    And a create group request with name "This group name is intentionally very long and exceeds the one hundred character limit set by the domain"
    And the create group join policy is "OPEN"
    When I submit the create group request
    Then I receive a 400 Bad Request response

  Scenario: Create group response contains description
    Given I am authenticated as "alice"
    And a create group request with name "Chess Club"
    And the create group description is "For chess enthusiasts"
    And the create group join policy is "REQUEST"
    When I submit the create group request
    Then I receive a 201 Created response
    And the group description is "For chess enthusiasts"

  Scenario: Get group details without authentication
    And a create group request with name "Ping Pong Club"
    And the create group join policy is "OPEN"
    When I submit the create group request
    Then I receive a 401 Unauthorized response

  Scenario: Get group details with unknown group id
    Given I am authenticated as "alice"
    When I request a non-existent group
    Then I receive a 404 Not Found response

  Scenario: List my groups without authentication
    When I access a protected endpoint without a token
    Then I receive a 401 Unauthorized response

  Scenario: Update group description as admin
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    And an update group request with description "Best club in town"
    When I submit the update group request
    Then I receive a 200 OK response
    And the group description is "Best club in town"

  Scenario: Update group as non-member returns not found (BR8)
    Given I am authenticated as "alice"
    And another user has created a group named "Secret Club"
    And an update group request with name "Hacked Name"
    When I submit the update group request
    Then I receive a 404 Not Found response

  Scenario: Update group without authentication
    When I access a protected endpoint without a token
    Then I receive a 401 Unauthorized response

  Scenario: Update group with name exceeding 100 characters
    Given I am authenticated as "alice"
    And I have created a group named "My Club" with join policy "OPEN"
    And an update group request with name "This group name is intentionally very long and exceeds the one hundred character limit set by the domain"
    When I submit the update group request
    Then I receive a 400 Bad Request response
```

---

## 5. Step Definition Notes

### New steps required — RegistrationSteps.java

| Step text | Action |
|---|---|
| `And the user profile contains an id` | New step — asserts response body has a non-null `user.id` field (navigating the AuthResponse's nested `user` object) |
| `And the user profile email is {string}` | New step — asserts `user.email` in the registration AuthResponse |

### New steps required — LoginSteps.java

| Step text | Action |
|---|---|
| `And the login response contains the user profile` | New step — asserts `user` sub-object is present with non-null `id` and `username` |
| `When I access a protected endpoint with a tampered token` | New step — send a JWT with a different signing key to `/users/me` |

### New steps required — ProfileSteps.java

| Step text | Action |
|---|---|
| `And a profile update request with bio {string}` | New step — sets `pendingBio` only (no username); note: conflicts with existing `And a profile update request with username {string}` if combined — must use a distinct Given that initialises pending fields differently. Suggest adding this as the "start" step: `Given a profile update request with bio {string}` |
| `And a profile update request with avatar url {string}` | New step — sets `pendingAvatarUrl` only |
| `And the profile contains an id` | New step — asserts `id` is non-null in response body |
| `And the profile email is {string}` | New step — asserts `email` field |
| `And the profile bio is {string}` | New step — asserts `bio` field |
| `And the profile avatar url is {string}` | New step — asserts `avatarUrl` field |
| `And the public profile does not contain an email` | New step — asserts no `email` key in the public profile response body |
| `Given a user with username {string} has deleted their account` | New step — registers a user by username, authenticates, calls DELETE /users/me. Note: `LoginSteps` has `a user with email {string} has deleted their account` — do NOT reuse that step; this uses username, not email |

### New steps required — GroupSteps.java

| Step text | Action |
|---|---|
| `And an update group request with description {string}` | New step — sets `pendingUpdateDescription` only (parallel to `an update group request with name {string}`) |
| `And the group description is {string}` | New step — asserts `description` field in response body |
| `When I request a non-existent group` | New step — sends GET `/groups/{randomUUID}` with auth token |

### Conflict warnings

1. **"And a profile update request with username {string}"** (ProfileSteps) vs the proposed `"And a profile update request with bio {string}"` — these use different parameter types and different step texts, so no ambiguity. However, the pending-field initialization pattern matters: the bio-only step must NOT reset `pendingUsername`. The current `aProfileUpdateRequestWithUsername` resets all pending fields. For bio-only and avatar-only scenarios, introduce distinct "starter" steps that do not reset the username, or use a common `pendingUpdateProfile` object approach.

2. **"When I access a protected endpoint without a token"** is declared in LoginSteps (hardcoded to `/users/me` GET). The suggested scenarios for profile update without auth and group without auth that reuse this step are intentionally reusing it — this is acceptable because the step tests that Spring Security blocks unauthenticated access, and `/users/me` GET is sufficient for that class of assertion. Do NOT add new `When I access ... without a token` steps for each endpoint — keep the shared step in CommonSteps or LoginSteps.

3. **"Given a create group request with name {string}"** (GroupSteps) — the suggested "Create group without authentication" scenario begins with this step without a prior `Given I am authenticated as`. That is intentional — the auth token in `ScenarioContext` is null/empty and `iSubmitTheCreateGroupRequest()` will send `Authorization: Bearer null`. The server rejects the token and returns 401. Verify `authorizedEntity()` in GroupSteps handles a null token gracefully (sends `Bearer null` string, which Spring Security will reject as an invalid token — this returns 401 as expected).

4. **"And another user has created a group named {string}"** exists (GroupSteps) and is reused in the "Update group as non-member" scenario. The step sets `currentGroupId` from the other user's group. The subsequent `When I submit the update group request` sends the update as the authenticated user (alice), who is not a member. This correctly exercises the non-member → 404 path. No new step needed.

---

## 6. Summary

### Counts

| Category | Count |
|---|---|
| Existing scenarios | 29 |
| Critical gaps | 5 |
| Important gaps | 8 |
| Nice-to-have gaps | 6 |
| New scenarios proposed | 31 |
| New step definitions needed | ~15 |

### Top priorities

1. Add 401 auth-guard scenarios for all Group endpoints and for `PUT /users/me`, `DELETE /users/me`, `GET /users/{username}` — these are one-liner scenarios that reuse the existing `When I access a protected endpoint without a token` step and require zero new step code.

2. Add the "Update group as non-member → 404" scenario (BR8 on the update path) — high business-rule severity, uses only existing steps.

3. Add login and registration validation boundary scenarios (blank fields, malformed email on login) — all use existing step vocabulary, zero new steps.

4. Add `And the group description is {string}` step and the corresponding scenario asserting the description is returned correctly — prevents silent mapper omissions.

5. Add `And the public profile does not contain an email` — a negative assertion that verifies the privacy boundary between `UserResponse` and `PublicUserResponse`.

### Step vocabulary health

The existing step vocabulary is clean and well-organised. The proposed new steps are additive — no existing steps need modification. The only structural change worth considering is refactoring the `pendingUpdate*` fields in `ProfileSteps` into a single `pendingUpdateRequest` builder object, so that bio-only and avatar-only update steps do not accidentally reset fields set by preceding steps.
