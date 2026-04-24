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
├── application/                   # Use cases, commands, ports
│   └── {context}/
│       ├── port/in/               # Inbound ports (e.g. RegisterUserPort)
│       ├── port/out/              # Outbound ports (e.g. UserRepositoryPort)
│       ├── usecase/               # Use case implementations
│       ├── command/               # Command objects (use case inputs)
│       ├── dto/                   # Request/Response DTOs (for controllers only)
│       └── mapper/                # Domain ↔ DTO mapping
├── infrastructure/                # Framework-dependent adapters
│   ├── adapter/in/web/            # REST controllers (inbound adapters)
│   ├── adapter/out/persistence/   # JPA repositories & entities (outbound adapters)
│   ├── configuration/             # Spring beans, security config
│   └── security/                  # JWT, password hashing
```

Layer-specific conventions are documented in each layer's own `CLAUDE.md`:
- `src/main/java/com/elo/domain/CLAUDE.md` — domain rules
- `src/main/java/com/elo/application/CLAUDE.md` — use case & port conventions
- `src/main/java/com/elo/infrastructure/CLAUDE.md` — controller, Lombok & adapter conventions
- `src/test/CLAUDE.md` — testing strategy & Cucumber patterns
- `src/main/resources/db/changelog/CLAUDE.md` — migration conventions

### Key Architecture Rules

- **Domain layer has ZERO dependencies** on Spring, JPA, or infrastructure (Lombok is allowed)
- Dependencies always point **inward** — infrastructure → application → domain
- Each bounded context has its own exceptions, ports, and repository interfaces

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
