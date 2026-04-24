# Application Layer

Orchestration layer — no Spring, no JPA, no HTTP concerns.

## Ports

- **Inbound** (`port/in/`): interfaces suffixed `Port` (e.g. `RegisterUserPort`) — contracts controllers depend on
- **Outbound** (`port/out/`): interfaces suffixed `Port` (e.g. `UserRepositoryPort`, `PasswordHasherPort`) — contracts infrastructure adapters implement

## Use Cases (`usecase/`)

- Implement inbound port interfaces
- **`execute()` must read like plain English** — extract guard clauses and non-trivial conditions into descriptively named private methods (e.g. `ensureUsernameIsAvailable()`, `findActiveUserByEmail()`, `isUsernameChanging()`)
- **Return domain objects**, never DTOs — DTO mapping is the controller's responsibility
- Must not know about JWT, HTTP, or any infrastructure concern

## Commands (`command/`)

Plain data carriers for use case inputs (e.g. `RegisterUserCommand`) — no mapping logic. Use cases call domain factory methods directly (e.g. `User.create(...)`).
