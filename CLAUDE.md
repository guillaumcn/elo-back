# CLAUDE.md — ELO Ranking Backend

## Project Overview

ELO Ranking App backend — a social platform for tracking skill ratings across competitive activities among friends. Users form groups, define activities, record matches, and get ELO-based rankings.

Full specs: [business-specifications.md](./business-specifications.md) and [technical-specifications.md](./technical-specifications.md).

## Tech Stack

- **Java 21**, **Spring Boot 3.4**, **Maven** (use `./mvnw`)
- **PostgreSQL 16** (via Docker Compose)
- **Liquibase** for migrations
- **Spring Security + JWT** (jjwt library) for auth
- **Lombok** for boilerplate reduction
- **Testcontainers**, **JUnit 5**, **Mockito**, **Cucumber** for testing

## Build & Run

```bash
# Start PostgreSQL
docker compose up -d

# Build (skip tests)
./mvnw package -DskipTests

# Run
./mvnw spring-boot:run

# Run tests (requires Docker for Testcontainers)
./mvnw test
```

## Architecture

**Hexagonal Architecture (Ports & Adapters)** with **Domain-Driven Design**.

```
com.elo
├── domain/                        # Pure business logic — NO framework dependencies
│   └── {context}/
│       ├── model/                 # Aggregates, Entities, Value Objects
│       └── exception/             # Domain exceptions
├── application/                   # Use cases, commands, DTOs, mappers
│   └── {context}/
│       ├── port/in/               # Inbound ports (use case interfaces, e.g. RegisterUserPort)
│       ├── port/out/              # Outbound ports (repository interfaces, e.g. UserRepositoryPort)
│       ├── usecase/               # Use case implementations (implement port/in interfaces)
│       ├── command/               # Command objects (use case inputs)
│       ├── dto/                   # Request/Response DTOs (for controllers only)
│       └── mapper/                # Domain ↔ DTO mapping
├── infrastructure/                # Framework-dependent adapters
│   ├── adapter/in/web/            # REST controllers (inbound adapters)
│   ├── adapter/out/persistence/   # JPA repositories & entities (outbound adapters)
│   ├── configuration/             # Spring beans, security config
│   └── security/                  # JWT, password hashing
```

### Lombok Conventions

- **`@RequiredArgsConstructor`** on all Spring beans and service classes (controllers, adapters, use cases) — replaces manual constructor injection
- **`@Getter` + `@Builder` on constructor** for domain models and JPA entities — prefer `@Builder` on the all-args constructor, not on the class
- **`@NoArgsConstructor(access = PROTECTED)`** on JPA entities (required by JPA)
- **`@NoArgsConstructor(access = PRIVATE)`** on utility/mapper classes — replaces private constructor
- **Do NOT use Lombok on records** — Java records already provide accessors, equals/hashCode, toString
- **Do NOT use `@Setter`** — entities mutate via explicit methods (e.g. `updateProfile()`, `deleteAccount()`), not setters; value objects are immutable records
- **Use builders in mappers** — prefer `.builder()...build()` over positional constructor calls for readability

### Key Architecture Rules

- **Domain layer has ZERO dependencies** on Spring, JPA, or infrastructure (Lombok is allowed as it has no runtime dependency)
- Dependencies always point **inward** — infrastructure → application → domain
- Each bounded context has its own exceptions, ports, and repository interfaces

### Layered Responsibilities

#### Domain Layer
- Contains **only** pure business logic: models, value objects, domain exceptions
- **Business validation belongs in the domain layer** — domain models must enforce their own invariants (e.g. non-blank username, valid email format, password length) in constructors or factory methods, throwing domain exceptions on violation. This ensures invariants hold regardless of which inbound adapter (REST, CLI, messaging, etc.) triggers the flow. Controllers may additionally validate request format, but domain rules must never rely solely on adapter-layer validation.
- **Entities vs value objects**: domain entities have identity and are mutable — mutation methods (e.g. `updateProfile()`, `deleteAccount()`) modify `this` directly and return `void`; value objects have no identity and are immutable — model them as Java records (e.g. `EloRating`, `MatchResult`)
- **No domain services** unless genuinely needed for cross-aggregate domain logic

#### Application Layer
- **Inbound ports** (`port/in/`): interfaces suffixed with `Port` (e.g. `RegisterUserPort`) — the contract controllers depend on
- **Outbound ports** (`port/out/`): interfaces suffixed with `Port` (e.g. `UserRepositoryPort`, `PasswordHasherPort`) — contracts that infrastructure adapters implement
- **Use cases** (`usecase/`): implement inbound port interfaces — contain orchestration logic (validation, calling domain, calling outbound ports)
- **Use cases must be maximally human-readable** — the `execute()` method should read like a plain-English description of the flow; extract guard clauses, lookups, and non-trivial conditions into descriptively named private methods (e.g. `ensureUsernameIsAvailable()`, `findActiveUserByEmail()`, `isUsernameChanging()`)
- **Commands** (`command/`): plain data carriers for use case inputs (e.g. `RegisterUserCommand`) — no mapping logic; use cases call domain factory methods directly (e.g. `User.create(...)`)
- **Use cases return domain objects**, never DTOs — DTO mapping is the controller's responsibility
- **Use cases must not know about JWT, HTTP, or any infrastructure concern**

#### Infrastructure Layer
- **Controllers** depend on inbound port interfaces (not on use case classes directly)
- **Controllers** are responsible for: request validation (via DTOs), calling the port/in, JWT generation, mapping domain objects to response DTOs
- **Request DTOs expose a `toCommand()` instance method** — controllers call `request.toCommand()` to get the command; no separate mapper needed for request → command mapping
- **`{Entity}ResponseMapper`** in `infrastructure/adapter/in/web/{context}/mapper/` maps domain objects to response DTOs — controllers call `{Entity}ResponseMapper.toResponse(domainObject)`
- **Controllers must have Swagger/OpenAPI annotations**: `@Tag` on the class, `@Operation` + `@ApiResponse` on each endpoint (include error responses with `ErrorResponse` schema)
- **Prefer `@ResponseStatus` over `ResponseEntity`** — keeps controller methods clean by returning DTOs directly instead of wrapping them
- **Outbound adapters** (JPA repositories) implement domain outbound port interfaces
- **Bean configuration** wires use cases to their port/in interfaces via `@Bean` methods

### Request Flow

```
Controller
  → request.toCommand() → Command
  → calls PortIn.execute(command)
    → UseCase (implements PortIn)
      → validates business rules via PortOut
      → Domain.create(...) → domain object
      → persists via PortOut
      → returns domain object
  ← receives domain object
  → generates JWT (if auth endpoint)
  → maps domain object to Response DTO
  → returns HTTP response
```

### Naming Conventions

| Concept | Convention | Example |
|---|---|---|
| Inbound port | `{Action}Port` | `RegisterUserPort` |
| Outbound port | `{Entity}RepositoryPort`, `{Action}Port` | `UserRepositoryPort`, `PasswordHasherPort` |
| Use case | `{Action}UseCase` | `RegisterUserUseCase` |
| Command | `{Action}Command` | `RegisterUserCommand` |
| Request DTO | `{Action}Request` | `RegisterRequest` |
| Response DTO | `{Entity}Response` | `UserResponse` |
| Response mapper | `{Entity}ResponseMapper` | `UserResponseMapper` |

## Bounded Contexts

| Context | Responsibility |
|---|---|
| **Identity** | User registration, authentication, profile |
| **Group** | Group lifecycle, membership, admin |
| **Activity** | Activity lifecycle, membership, catalog |
| **Match** | Match lifecycle, results, validation |
| **Ranking** | ELO calculation, leaderboards, stats |

## REST API

Base URL: `/api/v1`. All endpoints except `/auth/register` and `/auth/login` require JWT (`Authorization: Bearer <token>`).

## Database Migrations

- Liquibase changelogs organized by bounded context: `db/changelog/identity/`, `db/changelog/group/`, etc.
- Master changelog: `src/main/resources/db/changelog/db.changelog-master.yaml`
- **Each migration file is listed explicitly** in the master changelog (no `includeAll`) — this controls execution order and avoids date-prefixed filenames
- Naming convention: `NNN-description.yaml` (e.g. `001-create-users-table.yaml`)

## Testing Strategy

| Level | Scope | Tools |
|---|---|---|
| Unit | Use cases, ELO algorithm, value objects | JUnit 5, Mockito |
| Integration | Repository adapters, DB queries | Testcontainers, Spring Boot Test |
| API | REST controllers, request/response cycle | MockMvc/RestAssured, Testcontainers |
| Acceptance | End-to-end feature validation | Cucumber (Gherkin) + Spring Boot Test + Testcontainers |

- Domain and application layers tested in **pure isolation** (no Spring context)
- **Each class gets its own dedicated test class** — no global service tests bundling multiple concerns:
  - Domain models → `src/test/java/com/elo/domain/{context}/model/{Model}Test.java`
  - Use cases → `src/test/java/com/elo/application/{context}/usecase/{Action}UseCaseTest.java`
  - Commands → `src/test/java/com/elo/application/{context}/command/{Action}CommandTest.java`
  - Mappers → `src/test/java/com/elo/application/{context}/mapper/{Entity}MapperTest.java`
- **Test package mirrors source package** — e.g. `com.elo.domain.identity.model.User` is tested in `com.elo.domain.identity.model.UserTest`
- Acceptance test features: `src/test/resources/features/{identity,group,activity,match,ranking}/`
- Each acceptance scenario manages its own test data through API calls in `Given` steps
- ELO algorithm requires **dedicated exhaustive tests** (1v1, team, FFA, draws, cancellation revert)

### Acceptance Test Step Pattern

All acceptance scenarios follow this structure — build the request field by field, then submit:

```gherkin
Scenario: <description>
  Given <precondition — existing data or first request field if no precondition>
  And a <action> request with <field> "<value>"   # first request field (if precondition exists)
  And the <action> <field> is "<value>"           # each subsequent request field
  When I submit the <action> request
  Then I receive a <NNN> <Status> response
  And <additional assertions>
```

- **`Given`**: sets up preconditions (existing DB state) via API calls, OR starts the request building when there is no precondition
- **`And a X request with <field> "<value>"`**: declares the first field of the pending request
- **`And the X <field> is "<value>"`**: each additional field of the pending request
- **`When I submit the X request`**: executes the HTTP call using the pending fields
- Step classes hold `pending*` fields populated by `And` steps and consumed by `When`
- **Before writing any step**, search all existing step files for the same (or conflicting) step text — duplicates and parameterized vs. literal conflicts both cause `AmbiguousStepDefinitionsException` at runtime
- Generic status assertions (`I receive a 2xx/4xx response`) and shared setup steps (`a user exists with username`) live in `CommonSteps.java` — never redeclare them in feature step classes
- All step classes use `ScenarioContext` (a `@ScenarioScope` Spring bean) to share `response` and `authToken` across step classes within a scenario — step classes must never hold `response` as an instance field
- **Prefer controller-based steps for test context setup** — use existing or new Cucumber steps that call REST controllers (e.g. `Given a registered user with email "..." and password "..."` calls `/auth/register`); only fall back to direct database manipulation (e.g. via repositories or SQL) when no controller exists yet for that operation. Each time a new feature/controller is implemented, check whether existing database-level steps can be replaced with the new controller-based equivalent and update them.
- **Step text must be human-readable and business-focused** — never expose HTTP methods, URLs, or technical details in `.feature` files; only the step implementation (Java) knows which route to call (e.g. use `When I access a protected endpoint without a token`, not `When I call GET /api/v1/users/me without an Authorization header`)

## Error Handling

Standardized error response: `{ status, error, message, timestamp }`.

| HTTP Status | Usage |
|---|---|
| 400 | Validation errors |
| 401 | Missing/invalid JWT |
| 403 | Insufficient permissions |
| 404 | Resource not found |
| 409 | Conflict (username taken, already member) |
| 422 | Business rule violation (archived group, match already validated) |

## ELO Algorithm

- **1v1**: Standard ELO formula, K=32
- **Teams**: Average ELO per team, same delta for all team members
- **FFA**: Pairwise comparisons, average of all pairwise ELO changes
- **Winner-only mode**: Winner = rank 1, all others share rank 2
- **Cancellation**: Revert ELO deltas for all participants
