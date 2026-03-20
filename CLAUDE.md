# CLAUDE.md — ELO Ranking Backend

## Project Overview

ELO Ranking App backend — a social platform for tracking skill ratings across competitive activities among friends. Users form groups, define activities, record matches, and get ELO-based rankings.

Full specs: [business-specifications.md](./business-specifications.md) and [technical-specifications.md](./technical-specifications.md).

## Tech Stack

- **Java 21**, **Spring Boot 3.4**, **Maven** (use `./mvnw`)
- **PostgreSQL 16** (via Docker Compose)
- **Liquibase** for migrations
- **Spring Security + JWT** (jjwt library) for auth
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
├── domain/                    # Pure business logic — NO framework dependencies
│   ├── model/                 # Aggregates, Entities, Value Objects
│   ├── port/in/               # Inbound ports (use case interfaces)
│   ├── port/out/              # Outbound ports (repository interfaces)
│   ├── service/               # Domain services (use case implementations)
│   └── exception/             # Domain exceptions
├── application/               # DTOs, mappers
│   ├── dto/
│   └── mapper/
├── infrastructure/            # Framework-dependent adapters
│   ├── adapter/in/web/        # REST controllers
│   ├── adapter/out/persistence/ # JPA repositories & entities
│   ├── configuration/         # Spring beans, security config
│   ├── security/              # Security configuration
│   └── migration/             # Liquibase changelogs
```

### Key Rules

- **Domain layer has ZERO dependencies** on Spring, JPA, or infrastructure
- Dependencies always point **inward** — infrastructure depends on domain, never the reverse
- Inbound adapters (controllers) call inbound ports; outbound adapters (JPA repos) implement outbound ports
- Each bounded context has its own exceptions, ports, and repository interfaces

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
- Naming convention: `YYYY-MM-DD-NNN-description.yaml`

## Testing Strategy

| Level | Scope | Tools |
|---|---|---|
| Unit | Domain services, ELO algorithm, value objects | JUnit 5, Mockito |
| Integration | Repository adapters, DB queries | Testcontainers, Spring Boot Test |
| API | REST controllers, request/response cycle | MockMvc/RestAssured, Testcontainers |
| Acceptance | End-to-end feature validation | Cucumber (Gherkin) + Spring Boot Test + Testcontainers |

- Domain layer tested in **pure isolation** (no Spring context)
- Acceptance test features: `src/test/resources/features/{identity,group,activity,match,ranking}/`
- Each acceptance scenario manages its own test data through API calls in `Given` steps
- ELO algorithm requires **dedicated exhaustive tests** (1v1, team, FFA, draws, cancellation revert)

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
