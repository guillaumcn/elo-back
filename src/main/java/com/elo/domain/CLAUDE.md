# Domain Layer

Contains **only** pure business logic — NO Spring, JPA, or infrastructure dependencies (Lombok is allowed).

## Responsibilities

- Domain models (aggregates, entities, value objects), domain exceptions
- **Business validation belongs here** — models enforce their own invariants in constructors or factory methods, throwing domain exceptions on violation. This ensures invariants hold regardless of which adapter triggers the flow.

## Entities vs Value Objects

- **Entities**: have identity, are mutable — mutation methods (e.g. `updateProfile()`, `deleteAccount()`) modify `this` and return `void`
- **Value objects**: no identity, immutable — model as Java records (e.g. `EloRating`, `MatchResult`)

## Lombok on domain models

Use `@Getter` + `@Builder` on the all-args constructor. Do NOT use `@Setter`. Do NOT use Lombok on records.

## No domain services

Avoid domain services unless genuinely needed for cross-aggregate logic.
