---
name: implement-feature
description: Use this skill whenever the user asks to implement, add, or build a new feature, endpoint, use case, bounded context, or domain rule in this ELO backend. Triggers on phrases like "add a feature", "implement X", "create an endpoint to", "add a use case for", "build the Match context", "wire up the Y API", or any request that introduces new business behavior. It guides the implementation top-down across the hexagonal/DDD layers (domain → application → infrastructure → tests) by pointing at the nearest existing analog as a template, so the feature is built correctly without needing the whole codebase in context.
---

# Implement a Feature — Blueprint

This skill is a **map, not a textbook**. It tells you *which existing files to open as templates* and *in what order to create new files*. Read the references it points to — don't try to derive patterns from scratch.

Core docs already in your context via `CLAUDE.md`: layered responsibilities, Lombok rules, naming conventions, request flow, and test obligations. Rely on them. This skill fills the gap between "I know the rules" and "I know what to actually do next."

## Step 1 — Classify the feature

Every request falls into one of four shapes. Identify yours before touching the keyboard.

| Shape | Example | Scope |
|---|---|---|
| **A. New endpoint on an existing bounded context** | "Add a leave-group endpoint" | New use case + port + command + DTOs + controller method in an existing `{context}` package. No migration unless you add state. |
| **B. New entity inside an existing bounded context** | "Add GroupJoinRequest to the group context" | New domain model + outbound port + JPA entity/repo adapter + Liquibase migration + endpoints. |
| **C. New bounded context** | "Create the Match context" | Everything from scratch: new packages under `domain/`, `application/`, `infrastructure/adapter/out/persistence/{context}/`, new `{Context}BeanConfiguration`, new Liquibase subfolder, new `.feature` files, new step class. |
| **D. Change a domain rule** | "Group names must now be globally unique" | Domain model + affected use cases + migration (if persistence impact) + new tests. Often no new controller. |

If the user's request mixes shapes, implement them in the order above (A → D). Commit-sized units are fine.

## Step 2 — Read the nearest analog

Don't read the whole codebase. Open the **closest existing implementation** and mirror it.

### For shape A or B, the **Group context** is the canonical template

Before writing anything, read only the files relevant to the pieces you're adding:

- Use case + port + command → `src/main/java/com/elo/application/group/usecase/CreateGroupUseCase.java`, `port/in/CreateGroupPort.java`, `command/CreateGroupCommand.java`
- Use case that does validation (admin check, "already exists") → `application/group/usecase/ArchiveGroupUseCase.java` or `UpdateGroupUseCase.java`
- Request DTO with `toCommand()` → `application/group/dto/CreateGroupRequest.java`
- Response DTO + mapper → `application/group/dto/GroupResponse.java`, `infrastructure/adapter/in/web/group/mapper/GroupResponseMapper.java`
- Controller method (with Swagger, `@ResponseStatus`, `Authentication`) → `infrastructure/adapter/in/web/group/GroupController.java`
- Domain model with invariants + mutation methods → `domain/group/model/Group.java`
- Domain exception → `domain/group/exception/InvalidGroupException.java`
- Outbound port + JPA adapter → `application/group/port/out/GroupRepositoryPort.java`, `infrastructure/adapter/out/persistence/group/GroupRepositoryAdapter.java`
- JPA entity + persistence mapper → `infrastructure/adapter/out/persistence/group/GroupJpaEntity.java`, `GroupPersistenceMapper.java`
- Bean wiring → `infrastructure/configuration/GroupBeanConfiguration.java`
- Liquibase migration → `src/main/resources/db/changelog/group/001-create-groups-table.yaml` (and how it's registered in `db.changelog-master.yaml`)
- Unit test for a use case → `src/test/java/com/elo/application/group/usecase/CreateGroupUseCaseTest.java`
- Domain invariant tests → `src/test/java/com/elo/domain/group/model/GroupTest.java`
- Acceptance `.feature` → `src/test/resources/features/group/group_crud.feature`
- Acceptance steps → `src/test/java/com/elo/acceptance/group/GroupSteps.java`

### For shape C (new bounded context)

Mirror the entire `group` package tree. Create each sibling under `domain/{ctx}/`, `application/{ctx}/`, `infrastructure/adapter/out/persistence/{ctx}/`, `infrastructure/adapter/in/web/{ctx}/`. Add a `{Ctx}BeanConfiguration` alongside `GroupBeanConfiguration.java`.

### For shape D (domain rule change)

Open the relevant domain model and its test class. Fix the model, then fix whichever use cases now violate it. Check if a migration is needed (e.g., unique index).

## Step 3 — Create files in this order

Build inside-out so each layer compiles against what's below it and nothing above is orphaned.

1. **Liquibase migration** (only if persistence changes) — next numeric prefix in `db/changelog/{context}/NNN-description.yaml`, then register it explicitly in `db.changelog-master.yaml` (no `includeAll`).
2. **Domain model + exceptions** in `domain/{context}/` — invariants enforced in constructor or factory; mutation methods return `void`; value objects are `record`s; no Spring/JPA imports. Write `{Model}Test.java` now (invariants are the easy place to start TDD).
3. **Outbound port** in `application/{context}/port/out/` — interface only.
4. **Command** in `application/{context}/command/` — plain `record`, input carrier only.
5. **Inbound port** in `application/{context}/port/in/` — single-method interface suffixed `Port`.
6. **Use case** in `application/{context}/usecase/` — implements the inbound port; `execute()` reads like plain English; extract guards into private methods (`ensureUserIsAdmin(...)`, `findActiveGroup(...)`). Write `{Action}UseCaseTest.java` beside it (mock the outbound port with Mockito).
7. **Request DTO** in `application/{context}/dto/` — add `toCommand(...)` instance method; use `jakarta.validation` annotations.
8. **Response DTO** in `application/{context}/dto/` — immutable `record`.
9. **Response mapper** in `infrastructure/adapter/in/web/{context}/mapper/` — private constructor via `@NoArgsConstructor(access = PRIVATE)`, static `toResponse(domain)`.
10. **JPA entity** in `infrastructure/adapter/out/persistence/{context}/` — `@NoArgsConstructor(access = PROTECTED)`, `@Builder` on the all-args constructor, `@Getter`, no `@Setter`.
11. **Spring Data JPA repository** — empty interface extending `JpaRepository` (custom queries only if needed).
12. **Persistence mapper** — static `toDomain(entity)` / `toJpaEntity(domain)`; `@NoArgsConstructor(access = PRIVATE)`.
13. **Repository adapter** — `@Repository @RequiredArgsConstructor` implementing the outbound port, annotated `@Transactional` (or `@Transactional(readOnly = true)` for queries).
14. **Controller method** — depend on inbound port interface (never the use case class). Include full Swagger set: `@Operation`, `@ApiResponse` for each status the endpoint can return (including error responses with `ErrorResponse` schema). Prefer `@ResponseStatus` over `ResponseEntity`.
15. **Bean wiring** — add the `@Bean` method in the existing `{Context}BeanConfiguration`.
16. **Acceptance feature file** in `src/test/resources/features/{context}/` — follow the "build request field by field, then submit" pattern from `group_crud.feature`. Keep step text human/business-focused (no HTTP/URL leaking).
17. **Acceptance steps** — reuse `CommonSteps.java` for auth and generic status assertions; grep existing step files to avoid ambiguous step conflicts before adding anything. Share state via `ScenarioContext` (never instance fields on the step class).
18. **CucumberHooks** — if you added a new JPA repository, inject it into `CucumberHooks.java` and call `repository.deleteAll()` in the `@Before` hook. Missing this causes cross-scenario contamination that appears as flaky tests later.

## Step 4 — Conventions cheat-sheet (quick enforcement)

You won't remember every CLAUDE.md rule while coding. Scan this each time you're about to save a file.

**Domain layer**
- Zero Spring/JPA imports. Lombok only.
- Invariants thrown as `{Context}...Exception` (subclasses of an existing domain exception) — never `IllegalArgumentException`.
- Value objects → `record`. Entities → class with mutation methods returning `void`.
- No `@Service` here. Domain services only if genuinely needed for cross-aggregate logic.

**Application layer**
- Use cases return domain objects, never DTOs.
- Use cases must not import Spring, JWT, HTTP, or anything infrastructure.
- Commands have no mapping logic. Request DTOs own `toCommand()`.
- Use case `execute()` must read top-to-bottom like a paragraph. Extract helpers with descriptive names.

**Infrastructure layer**
- Controller depends on `PortIn` interface, not `UseCase` class.
- JWT generation / auth principal lookup lives in the controller, not the use case.
- Every endpoint has Swagger annotations including error responses (`400/401/403/404/409/422` as applicable) with `ErrorResponse` schema.
- Prefer `@ResponseStatus` + return the DTO directly over `ResponseEntity`.

**Lombok**
- `@RequiredArgsConstructor` on Spring beans (controllers, use cases, adapters).
- `@Getter` + `@Builder` on the **constructor** (not the class) for domain models and JPA entities.
- `@NoArgsConstructor(access = PROTECTED)` on JPA entities; `@NoArgsConstructor(access = PRIVATE)` on utility/mapper classes.
- No Lombok on `record`s. No `@Setter` anywhere.

**Error codes** (pick the right HTTP status)
- `400` validation (bean validation failure)
- `401` missing/invalid JWT
- `403` authenticated but not permitted (e.g., non-admin acting on group)
- `404` resource not found *or* user can't see it (don't leak existence)
- `409` conflict (username taken, already member)
- `422` business rule violation (archived group, match already validated)

## Step 5 — Verify, don't trust

Before declaring done:

1. Run the unit suite: `./mvnw test -B`
2. Run the acceptance suite: `./mvnw test -B -Dtest='com.elo.acceptance.CucumberTest'`
3. Grep for ambiguous/duplicated Cucumber steps: `grep -rn "@Given\|@When\|@Then\|@And" src/test/java/com/elo/acceptance/` and sanity-check your new steps don't collide with existing ones.
4. Confirm new JPA repos are deleted in `CucumberHooks#cleanDatabase()`.
5. Confirm your new Liquibase file is explicitly listed in `db.changelog-master.yaml`.
6. Confirm your new use case has a `@Bean` entry in `{Context}BeanConfiguration`.
7. Confirm the new controller method has `@Operation` + `@ApiResponse` for every status it can return.

If any of these fail, fix and re-run — don't ship partial work.

## Step 6 — When to stop and ask

Pause and surface the question to the user if:
- The feature implies cross-context coupling (e.g., Match needs to read Group membership) — confirm whether this is a shared model, a duplicated read, or an event.
- A new HTTP status or error category doesn't fit the table in step 4.
- The domain rule change in shape D might break historical data and a backfill is needed.
- The request is ambiguous about authorization (member vs. admin vs. owner).

Short clarification beats a large rework.

## Anti-patterns to refuse

- Returning DTOs from use cases.
- Controllers calling `UseCase` classes directly instead of `PortIn` interfaces.
- Business validation in controllers only (invariants must be on the domain).
- `@Setter` on entities, or mutation methods that return `this`.
- Using `ResponseEntity` just to set the status — use `@ResponseStatus`.
- Exposing HTTP verbs or URLs in `.feature` files.
- Holding `response` or `authToken` as a field on a step class — use `ScenarioContext`.
- Adding an `includeAll` to the Liquibase master changelog — each file is listed explicitly.
- Skipping `CucumberHooks` updates when adding a new JPA repository.

---

**You have everything you need.** Classify → read the one or two analog files → build inside-out → run both test suites → ship.
