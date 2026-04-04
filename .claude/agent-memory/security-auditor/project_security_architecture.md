---
name: ELO Backend Security Architecture
description: Key security decisions, JWT config, Spring Security setup, and auth patterns discovered during full codebase audit (April 2026)
type: project
---

## JWT Configuration
- Library: jjwt 0.12.6
- Algorithm: HMAC-SHA (auto-selected by `Keys.hmacShaKeyFor` — resolves to HS256/HS384/HS512 based on key length)
- Secret source: environment variable `JWT_SECRET`, falls back to hardcoded default `default-dev-secret-change-in-production` (39 bytes — satisfies HS256 minimum of 32 bytes)
- `@PostConstruct` validation in `JwtTokenProvider` throws `IllegalStateException` if secret < 32 bytes
- Access token expiry: 900,000 ms (15 minutes) — appropriate
- Token claims: `sub` (userId UUID), `username`, `iat`, `exp` — all present and validated by jjwt parser
- Refresh token flow not implemented; the orphaned `refreshTokenExpirationMs` property was removed in April 2026

## Spring Security Filter Chain
- CSRF disabled (stateless JWT API — acceptable)
- Session creation policy: STATELESS
- Public endpoints: `/api/v1/auth/**` (register + login), `/actuator/health`, Swagger UI routes
- All other requests require JWT authentication
- Auth entrypoint returns HTTP 401 on authentication failure
- CORS: explicit `CorsConfigurationSource` bean; allowed origins from `cors.allowed-origins` property (defaults to common localhost dev ports); configured per-method and per-header

## Password Hashing
- BCrypt via Spring Security `BCryptPasswordEncoder` — strong adaptive algorithm, correct

## Active Bounded Contexts
- Identity: full CRUD (register, login, get profile, update profile, delete account, get public profile)
- Group: create, list, get, update, archive, unarchive — no delete yet

## Group Archive/Unarchive Authorization Pattern (April 2026)
- Both ArchiveGroupUseCase and UnarchiveGroupUseCase enforce ADMIN role check via `GroupMemberRepositoryPort.existsByGroupIdAndUserIdAndRole()`
- IDOR-safe: non-members get 404, members without ADMIN role get 403 — confirms existing UpdateGroupUseCase pattern
- Group state transitions enforced at domain level: `group.archive()` / `group.unarchive()` throw `GroupAlreadyArchivedException` / `GroupNotArchivedException` → 422
- Controller endpoints use `@PostMapping` for archive/unarchive (state-mutating actions), which is correct
- `archiveGroup` and `unarchiveGroup` controller methods intentionally omit `@ResponseStatus` (defaults to 200 OK), consistent with `updateGroup`

## Known Stub / Temporary Implementations
- `AlwaysVisibleUserVisibilityAdapter`: always returns `true`, guarded with `@Profile("!prod")` — not active in production

## Input Validation Constraints (as of April 2026)
- `UpdateProfileRequest.bio`: `@Size(max=500)` + domain invariant in `User.updateProfile()`
- `UpdateProfileRequest.avatarUrl`: `@Size(max=500)` + `@Pattern(regexp="^https://.*")` (HTTPS only)
- `CreateGroupRequest.description` / `UpdateGroupRequest.description`: `@Size(max=1000)` + domain invariant in `Group.update()`
- DB migrations applied: `identity-002` (bio varchar(500)), `group-003` (description varchar(1000))

## Error Handling
- `GlobalExceptionHandler` handles `DataIntegrityViolationException` → 409 CONFLICT with generic message (race condition safety net)
- Conflict exceptions (`UsernameAlreadyTakenException`, `EmailAlreadyTakenException`) return generic messages, not echoing user input

## Swagger / API Docs
- Enabled by default; disabled in `application-prod.yml` via `springdoc.api-docs.enabled: false` and `springdoc.swagger-ui.enabled: false`

**Why:** Recorded to preserve institutional knowledge across audit sessions.
**How to apply:** Use as baseline for future incremental audits — focus on delta since this snapshot.
