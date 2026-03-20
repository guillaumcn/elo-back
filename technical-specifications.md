# ELO Ranking App — Backend Technical Specifications

## Context

This document describes the high-level backend architecture and technical requirements for the ELO Ranking App. It follows a **Domain-Driven Design (DDD)** approach embedded in a **Hexagonal Architecture** (Ports & Adapters). The backend exposes a REST API consumed by web and mobile clients.

Reference: [Business Specifications](./business-specifications.md)

---

## 1. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21+ |
| Framework | Spring Boot 3+ |
| Database | PostgreSQL |
| Migrations | Liquibase |
| Containerization | Docker |
| Orchestration | Kubernetes |
| Build tool | Maven or Gradle |
| API format | REST (JSON) |
| Authentication | Spring Security + JWT |
| Testing | JUnit 5, Mockito, Testcontainers |

---

## 2. Hexagonal Architecture

### Structure

```
com.elo
├── domain/                    # Pure business logic — no framework dependencies
│   ├── model/                 # Aggregates, Entities, Value Objects
│   ├── port/
│   │   ├── in/               # Inbound ports (use cases interfaces)
│   │   └── out/              # Outbound ports (repository interfaces, external services)
│   ├── service/              # Domain services (use case implementations)
│   └── exception/            # Domain exceptions
│
├── application/              # Application layer — orchestration, DTOs
│   ├── dto/                  # Request/Response DTOs
│   └── mapper/               # Domain ↔ DTO mapping
│
├── infrastructure/           # Adapters — framework-dependent implementations
│   ├── adapter/
│   │   ├── in/
│   │   │   └── web/         # REST controllers (inbound adapters)
│   │   └── out/
│   │       └── persistence/ # JPA repositories, entities, mappers (outbound adapters)
│   ├── configuration/       # Spring beans, security config
│   └── migration/           # Liquibase changelogs
```

### Rules

- The **domain layer** has zero dependencies on Spring, JPA, or any infrastructure framework
- **Inbound ports** define the use cases the domain exposes (interfaces)
- **Outbound ports** define what the domain needs from the outside (repository interfaces)
- **Inbound adapters** (controllers) call inbound ports
- **Outbound adapters** (JPA repositories) implement outbound ports
- Dependencies always point **inward** — infrastructure depends on domain, never the reverse

---

## 3. Bounded Contexts & Domain Model

### 3.1 Bounded Contexts

| Bounded Context | Responsibility |
|---|---|
| **Identity** | User registration, authentication, profile management |
| **Group** | Group lifecycle, membership, administration |
| **Activity** | Activity lifecycle, membership, configuration, catalog |
| **Match** | Match lifecycle, result recording, validation |
| **Ranking** | ELO calculation, leaderboards, player statistics |

### 3.2 Aggregates

#### Identity Context

**User** (Aggregate Root)
- `id: UUID`
- `username: String` (unique)
- `email: String` (unique)
- `passwordHash: String`
- `avatarUrl: String`
- `bio: String` (optional)
- `createdAt: Instant`
- `updatedAt: Instant`
- `deleted: boolean`

#### Group Context

**Group** (Aggregate Root)
- `id: UUID`
- `name: String`
- `description: String`
- `joinPolicy: JoinPolicy` (OPEN, INVITATION, REQUEST)
- `archived: boolean`
- `createdAt: Instant`
- `createdBy: UUID`

**GroupMembership** (Entity within Group or standalone)
- `id: UUID`
- `groupId: UUID`
- `userId: UUID`
- `role: GroupRole` (MEMBER, ADMIN)
- `joinedAt: Instant`

**GroupJoinRequest** (Entity)
- `id: UUID`
- `groupId: UUID`
- `userId: UUID`
- `status: RequestStatus` (PENDING, APPROVED, DENIED)
- `requestedAt: Instant`
- `resolvedBy: UUID` (nullable)
- `resolvedAt: Instant` (nullable)

**GroupInvitation** (Entity)
- `id: UUID`
- `groupId: UUID`
- `invitedBy: UUID`
- `token: String` (unique invite link/code)
- `createdAt: Instant`
- `expiresAt: Instant` (nullable)

#### Activity Context

**Activity** (Aggregate Root)
- `id: UUID`
- `groupId: UUID`
- `name: String`
- `catalogEntryId: UUID` (nullable — null if custom)
- `startingElo: int` (immutable after creation)
- `joinPolicy: JoinPolicy` (OPEN, INVITATION, REQUEST)
- `validationMode: ValidationMode` (ALL_PARTICIPANTS, ANY_PARTICIPANT, CREATOR_ONLY)
- `archived: boolean`
- `createdAt: Instant`
- `createdBy: UUID`

**ActivityMembership** (Entity)
- `id: UUID`
- `activityId: UUID`
- `userId: UUID`
- `role: ActivityRole` (MEMBER, ADMIN)
- `joinedAt: Instant`

**ActivityCatalogEntry** (Entity)
- `id: UUID`
- `name: String`
- `iconUrl: String` (nullable)

#### Match Context

**Match** (Aggregate Root)
- `id: UUID`
- `activityId: UUID`
- `resultMode: ResultMode` (FULL_RANKING, WINNER_ONLY)
- `status: MatchStatus` (CREATED, RESULT_SET, VALIDATED, CANCELLED)
- `createdBy: UUID`
- `createdAt: Instant`
- `validatedAt: Instant` (nullable)
- `cancelledAt: Instant` (nullable)
- `cancelledBy: UUID` (nullable)

**MatchSide** (Entity within Match)
- `id: UUID`
- `matchId: UUID`
- `sideIndex: int` (team/side identifier)
- `rank: Integer` (nullable — set when result is recorded, 1 = winner)

**MatchParticipant** (Entity within Match)
- `id: UUID`
- `matchId: UUID`
- `sideId: UUID`
- `userId: UUID`
- `hasValidated: boolean`
- `eloBeforeMatch: Integer` (nullable — set at validation)
- `eloAfterMatch: Integer` (nullable — set at validation)

#### Ranking Context

**PlayerRanking** (Aggregate Root)
- `id: UUID`
- `activityId: UUID`
- `userId: UUID`
- `currentElo: int`
- `wins: int`
- `losses: int`
- `draws: int`
- `updatedAt: Instant`

**EloHistoryEntry** (Entity)
- `id: UUID`
- `playerRankingId: UUID`
- `matchId: UUID`
- `eloBefore: int`
- `eloAfter: int`
- `recordedAt: Instant`

---

## 4. Domain Value Objects

| Value Object | Fields | Used In |
|---|---|---|
| `JoinPolicy` | Enum: OPEN, INVITATION, REQUEST | Group, Activity |
| `ValidationMode` | Enum: ALL_PARTICIPANTS, ANY_PARTICIPANT, CREATOR_ONLY | Activity |
| `ResultMode` | Enum: FULL_RANKING, WINNER_ONLY | Match |
| `MatchStatus` | Enum: CREATED, RESULT_SET, VALIDATED, CANCELLED | Match |
| `GroupRole` | Enum: MEMBER, ADMIN | GroupMembership |
| `ActivityRole` | Enum: MEMBER, ADMIN | ActivityMembership |
| `RequestStatus` | Enum: PENDING, APPROVED, DENIED | GroupJoinRequest, ActivityJoinRequest |

---

## 5. Use Cases (Inbound Ports)

### Identity

| Use Case | Description |
|---|---|
| `RegisterUser` | Create account with email/password |
| `AuthenticateUser` | Login, return JWT |
| `GetUserProfile` | Get profile by ID or username |
| `UpdateUserProfile` | Update username, avatar, bio |
| `DeleteUserAccount` | Soft-delete and anonymize user data |

### Group

| Use Case | Description |
|---|---|
| `CreateGroup` | Create group, creator becomes admin |
| `GetGroup` | Get group details (members only) |
| `UpdateGroup` | Update name, description, join policy (admins) |
| `ArchiveGroup` | Archive/unarchive group (admins) |
| `JoinGroup` | Join open group directly |
| `RequestToJoinGroup` | Submit join request for request-to-join groups |
| `HandleJoinRequest` | Approve/deny a join request (admins) |
| `CreateGroupInvitation` | Generate invite link/code (admins/members) |
| `JoinGroupByInvitation` | Join via invite token |
| `PromoteMemberToAdmin` | Promote group member to admin (admins) |
| `StepDownAsAdmin` | Admin demotes themselves to member |
| `RemoveGroupMember` | Remove a member from group (admins) |
| `LeaveGroup` | Member leaves group voluntarily |
| `ListUserGroups` | List all groups for the authenticated user |

### Activity

| Use Case | Description |
|---|---|
| `CreateActivity` | Create activity in group, creator becomes admin |
| `GetActivity` | Get activity details (activity members only) |
| `UpdateActivity` | Update name, join policy, validation mode (admins) |
| `ArchiveActivity` | Archive/unarchive activity (admins) |
| `JoinActivity` | Join open activity directly |
| `RequestToJoinActivity` | Submit join request |
| `HandleActivityJoinRequest` | Approve/deny (activity admins) |
| `InviteToActivity` | Invite a group member to the activity |
| `PromoteActivityMember` | Promote to activity admin (activity admins) |
| `StepDownAsActivityAdmin` | Admin demotes themselves |
| `RemoveActivityMember` | Remove member from activity (activity admins) |
| `LeaveActivity` | Member leaves activity voluntarily |
| `ListGroupActivities` | List all activities in a group |
| `GetActivityCatalog` | List available catalog entries |

### Match

| Use Case | Description |
|---|---|
| `CreateMatch` | Create match with sides and participants |
| `SetMatchResult` | Record result (ranks per side) |
| `ValidateMatch` | Participant validates the result |
| `CancelMatch` | Activity admin cancels a match |
| `GetMatch` | Get match details |
| `ListActivityMatches` | List matches in an activity (with filters) |

### Ranking

| Use Case | Description |
|---|---|
| `GetActivityLeaderboard` | Get ranked list of players with ELO and win/loss ratio |
| `GetPlayerStats` | Get detailed stats for a player in an activity |
| `GetPlayerEloHistory` | Get ELO progression over time |
| `GetHeadToHead` | Get head-to-head record between two players |

---

## 6. Outbound Ports (Repository Interfaces)

Each bounded context defines its own repository interfaces in the domain layer:

- `UserRepository`
- `GroupRepository`, `GroupMembershipRepository`, `GroupJoinRequestRepository`, `GroupInvitationRepository`
- `ActivityRepository`, `ActivityMembershipRepository`, `ActivityJoinRequestRepository`, `ActivityCatalogRepository`
- `MatchRepository`, `MatchParticipantRepository`
- `PlayerRankingRepository`, `EloHistoryRepository`

These are implemented by JPA-backed adapters in the infrastructure layer.

---

## 7. ELO Algorithm

### Approach: Multi-player Glicko-2 Variant

The standard ELO system handles 1v1 well but struggles with team and multi-player matches. We use an adapted approach:

### 1v1 Matches
Standard ELO formula:
- **Expected score:** `E = 1 / (1 + 10^((Rb - Ra) / 400))`
- **New rating:** `Ra' = Ra + K * (S - E)`
- **K-factor:** 32 (can be adjusted per activity in the future)
- **S:** 1 for win, 0.5 for draw, 0 for loss

### Team Matches (2v2, 3v3…)
- Each team's effective rating = **average ELO of its members**
- The match is treated as a 1v1 between the two team ratings
- Each player on the winning/losing team receives the **same ELO delta**

### Free-for-All (1v1v1v1…)
- Each player is compared **pairwise** against every other player
- For each pair, expected scores are calculated
- Actual score: based on relative placement (higher rank = win, same rank = draw, lower rank = loss)
- ELO change = **average of all pairwise ELO changes**

### Winner-Only Mode
- The winner is treated as rank 1
- All other players share rank 2 (treated as a draw among themselves, loss against the winner)

### ELO Revert on Cancellation
- When a validated match is cancelled, each participant's `currentElo` is recalculated by removing the cancelled match's delta
- `EloHistoryEntry` for the cancelled match is marked as reverted

---

## 8. REST API Design

### Base URL
`/api/v1`

### Authentication
All endpoints except registration and login require a valid JWT in the `Authorization: Bearer <token>` header.

### Endpoints Overview

#### Auth
| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Authenticate, return JWT |

#### Users
| Method | Path | Description |
|---|---|---|
| GET | `/users/me` | Get current user profile |
| PUT | `/users/me` | Update profile |
| DELETE | `/users/me` | Delete account |
| GET | `/users/{username}` | Get public profile (if shared group) |

#### Groups
| Method | Path | Description |
|---|---|---|
| POST | `/groups` | Create group |
| GET | `/groups` | List user's groups |
| GET | `/groups/{groupId}` | Get group details |
| PUT | `/groups/{groupId}` | Update group |
| POST | `/groups/{groupId}/archive` | Archive group |
| POST | `/groups/{groupId}/unarchive` | Unarchive group |
| POST | `/groups/{groupId}/join` | Join open group |
| POST | `/groups/{groupId}/join-requests` | Request to join |
| GET | `/groups/{groupId}/join-requests` | List pending requests (admins) |
| PUT | `/groups/{groupId}/join-requests/{requestId}` | Approve/deny request |
| POST | `/groups/{groupId}/invitations` | Create invitation |
| POST | `/groups/{groupId}/join-by-invitation` | Join via invite token |
| GET | `/groups/{groupId}/members` | List members |
| DELETE | `/groups/{groupId}/members/{userId}` | Remove member (admin) or leave (self) |
| POST | `/groups/{groupId}/members/{userId}/promote` | Promote to admin |
| POST | `/groups/{groupId}/members/me/step-down` | Step down as admin |

#### Activities
| Method | Path | Description |
|---|---|---|
| POST | `/groups/{groupId}/activities` | Create activity |
| GET | `/groups/{groupId}/activities` | List group activities |
| GET | `/activities/{activityId}` | Get activity details |
| PUT | `/activities/{activityId}` | Update activity |
| POST | `/activities/{activityId}/archive` | Archive activity |
| POST | `/activities/{activityId}/unarchive` | Unarchive activity |
| POST | `/activities/{activityId}/join` | Join open activity |
| POST | `/activities/{activityId}/join-requests` | Request to join |
| GET | `/activities/{activityId}/join-requests` | List pending requests |
| PUT | `/activities/{activityId}/join-requests/{requestId}` | Approve/deny |
| POST | `/activities/{activityId}/invitations` | Invite member |
| GET | `/activities/{activityId}/members` | List members |
| DELETE | `/activities/{activityId}/members/{userId}` | Remove member or leave |
| POST | `/activities/{activityId}/members/{userId}/promote` | Promote to admin |
| POST | `/activities/{activityId}/members/me/step-down` | Step down |
| GET | `/activities/catalog` | Get activity catalog |

#### Matches
| Method | Path | Description |
|---|---|---|
| POST | `/activities/{activityId}/matches` | Create match |
| GET | `/activities/{activityId}/matches` | List activity matches |
| GET | `/matches/{matchId}` | Get match details |
| PUT | `/matches/{matchId}/result` | Set match result |
| POST | `/matches/{matchId}/validate` | Validate match result |
| POST | `/matches/{matchId}/cancel` | Cancel match (admin) |

#### Rankings & Stats
| Method | Path | Description |
|---|---|---|
| GET | `/activities/{activityId}/leaderboard` | Get leaderboard |
| GET | `/activities/{activityId}/players/{userId}/stats` | Get player stats |
| GET | `/activities/{activityId}/players/{userId}/elo-history` | Get ELO history |
| GET | `/activities/{activityId}/players/{userId}/head-to-head/{opponentId}` | Head-to-head |

---

## 9. Database Schema Overview

### Tables

```
users
  id (UUID, PK)
  username (VARCHAR, UNIQUE)
  email (VARCHAR, UNIQUE)
  password_hash (VARCHAR)
  avatar_url (VARCHAR, nullable)
  bio (TEXT, nullable)
  deleted (BOOLEAN, default false)
  created_at (TIMESTAMP)
  updated_at (TIMESTAMP)

groups
  id (UUID, PK)
  name (VARCHAR)
  description (TEXT, nullable)
  join_policy (VARCHAR) -- OPEN, INVITATION, REQUEST
  archived (BOOLEAN, default false)
  created_by (UUID, FK → users)
  created_at (TIMESTAMP)

group_memberships
  id (UUID, PK)
  group_id (UUID, FK → groups)
  user_id (UUID, FK → users)
  role (VARCHAR) -- MEMBER, ADMIN
  joined_at (TIMESTAMP)
  UNIQUE(group_id, user_id)

group_join_requests
  id (UUID, PK)
  group_id (UUID, FK → groups)
  user_id (UUID, FK → users)
  status (VARCHAR) -- PENDING, APPROVED, DENIED
  requested_at (TIMESTAMP)
  resolved_by (UUID, FK → users, nullable)
  resolved_at (TIMESTAMP, nullable)

group_invitations
  id (UUID, PK)
  group_id (UUID, FK → groups)
  invited_by (UUID, FK → users)
  token (VARCHAR, UNIQUE)
  created_at (TIMESTAMP)
  expires_at (TIMESTAMP, nullable)

activity_catalog
  id (UUID, PK)
  name (VARCHAR)
  icon_url (VARCHAR, nullable)

activities
  id (UUID, PK)
  group_id (UUID, FK → groups)
  name (VARCHAR)
  catalog_entry_id (UUID, FK → activity_catalog, nullable)
  starting_elo (INTEGER)
  join_policy (VARCHAR)
  validation_mode (VARCHAR) -- ALL_PARTICIPANTS, ANY_PARTICIPANT, CREATOR_ONLY
  archived (BOOLEAN, default false)
  created_by (UUID, FK → users)
  created_at (TIMESTAMP)

activity_memberships
  id (UUID, PK)
  activity_id (UUID, FK → activities)
  user_id (UUID, FK → users)
  role (VARCHAR) -- MEMBER, ADMIN
  joined_at (TIMESTAMP)
  UNIQUE(activity_id, user_id)

activity_join_requests
  id (UUID, PK)
  activity_id (UUID, FK → activities)
  user_id (UUID, FK → users)
  status (VARCHAR)
  requested_at (TIMESTAMP)
  resolved_by (UUID, FK → users, nullable)
  resolved_at (TIMESTAMP, nullable)

matches
  id (UUID, PK)
  activity_id (UUID, FK → activities)
  result_mode (VARCHAR) -- FULL_RANKING, WINNER_ONLY
  status (VARCHAR) -- CREATED, RESULT_SET, VALIDATED, CANCELLED
  created_by (UUID, FK → users)
  created_at (TIMESTAMP)
  validated_at (TIMESTAMP, nullable)
  cancelled_at (TIMESTAMP, nullable)
  cancelled_by (UUID, FK → users, nullable)

match_sides
  id (UUID, PK)
  match_id (UUID, FK → matches)
  side_index (INTEGER)
  rank (INTEGER, nullable)

match_participants
  id (UUID, PK)
  match_id (UUID, FK → matches)
  side_id (UUID, FK → match_sides)
  user_id (UUID, FK → users)
  has_validated (BOOLEAN, default false)
  elo_before_match (INTEGER, nullable)
  elo_after_match (INTEGER, nullable)

player_rankings
  id (UUID, PK)
  activity_id (UUID, FK → activities)
  user_id (UUID, FK → users)
  current_elo (INTEGER)
  wins (INTEGER, default 0)
  losses (INTEGER, default 0)
  draws (INTEGER, default 0)
  updated_at (TIMESTAMP)
  UNIQUE(activity_id, user_id)

elo_history
  id (UUID, PK)
  player_ranking_id (UUID, FK → player_rankings)
  match_id (UUID, FK → matches)
  elo_before (INTEGER)
  elo_after (INTEGER)
  reverted (BOOLEAN, default false)
  recorded_at (TIMESTAMP)
```

### Liquibase

- Changelogs organized by bounded context: `db/changelog/identity/`, `db/changelog/group/`, etc.
- Master changelog at `db/changelog/db.changelog-master.yaml` includes all sub-changelogs
- Each migration file follows the naming convention: `YYYY-MM-DD-NNN-description.yaml`

---

## 10. Security

### Authentication
- **JWT-based** stateless authentication
- Access token + refresh token pattern
- Passwords hashed with **bcrypt**
- JWT signed with RS256 or HS256

### Authorization
- All endpoints enforce **membership checks** — users can only access groups/activities they belong to
- Admin-only actions are enforced at the domain level (use case checks the caller's role)
- Match operations verify the caller is a participant or activity admin as appropriate

### Account Deletion
- Soft delete: `deleted = true`
- Anonymize personal data: username → `deleted_<uuid>`, email cleared, avatar cleared, bio cleared
- Foreign keys (match participants, memberships) are preserved with the anonymized user ID

---

## 11. Error Handling

### Domain Exceptions
Each bounded context defines its own exceptions:
- `UserNotFoundException`, `UsernameAlreadyTakenException`
- `GroupNotFoundException`, `NotGroupMemberException`, `NotGroupAdminException`, `GroupArchivedException`
- `ActivityNotFoundException`, `NotActivityMemberException`, `ActivityArchivedException`
- `MatchNotFoundException`, `MatchAlreadyValidatedException`, `InvalidMatchResultException`

### HTTP Error Responses
Standardized error response format:
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Group not found",
  "timestamp": "2026-03-20T10:30:00Z"
}
```

| HTTP Status | Usage |
|---|---|
| 400 | Validation errors, invalid input |
| 401 | Missing or invalid JWT |
| 403 | Insufficient permissions (not admin, not member) |
| 404 | Resource not found |
| 409 | Conflict (username taken, already a member, etc.) |
| 422 | Business rule violation (archived group, match already validated) |

---

## 12. Testing Strategy

| Level | Scope | Tools |
|---|---|---|
| **Unit tests** | Domain services, ELO algorithm, value objects | JUnit 5, Mockito |
| **Integration tests** | Repository adapters, database queries | Testcontainers (PostgreSQL), Spring Boot Test |
| **API tests** | REST controllers, full request/response cycle | MockMvc or RestAssured, Testcontainers |
| **Acceptance tests** | End-to-end feature validation against acceptance criteria | Cucumber (Gherkin) + Spring Boot Test + Testcontainers |

- Domain layer tested in **pure isolation** (no Spring context)
- Integration tests use **Testcontainers** for real PostgreSQL instances
- ELO algorithm must have **dedicated exhaustive tests** (1v1, team, FFA, draws, cancellation revert)

### Acceptance Tests

Each feature ticket includes **acceptance criteria** written in Gherkin syntax (Given/When/Then). These criteria serve as the source of truth for feature validation.

- **Framework:** Cucumber for Java, integrated with Spring Boot Test
- **Execution:** Run against a real application context with Testcontainers (PostgreSQL)
- **Scope:** Each acceptance test exercises the full stack — from REST API call to database — to validate business behavior end-to-end
- **Organization:** Feature files organized by bounded context (`src/test/resources/features/identity/`, `features/group/`, `features/activity/`, `features/match/`, `features/ranking/`)
- **Lifecycle:** Acceptance criteria are defined in the ticket before development starts. The corresponding Cucumber scenarios are implemented as part of the feature development and must all pass before the feature is considered done.
- **Data setup:** Each scenario manages its own test data through API calls in `Given` steps, ensuring tests are self-contained and independent

---

## 13. Deployment

### Docker
- Multi-stage Dockerfile: build with Maven/Gradle, run with minimal JRE image
- Environment-specific configuration via Spring profiles (`application-dev.yml`, `application-prod.yml`)
- Database connection configured via environment variables

### Kubernetes
- Deployment with readiness and liveness probes (`/actuator/health`)
- ConfigMap for non-sensitive configuration
- Secrets for database credentials and JWT signing keys
- Horizontal Pod Autoscaler based on CPU/memory
- PostgreSQL deployed separately (managed service or StatefulSet)
