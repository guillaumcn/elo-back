# Infrastructure Layer

Framework-dependent adapters — the only place Spring, JPA, JWT, and HTTP concerns live.

## Lombok Conventions

- `@RequiredArgsConstructor` on all Spring beans (controllers, adapters, use cases) — replaces manual constructor injection
- `@Getter` + `@Builder` on all-args constructor for JPA entities
- `@NoArgsConstructor(access = PROTECTED)` on JPA entities (required by JPA)
- `@NoArgsConstructor(access = PRIVATE)` on utility/mapper classes
- Do NOT use `@Setter` — entities mutate via explicit methods
- Do NOT use Lombok on records
- Use builders in mappers — prefer `.builder()...build()` over positional constructor calls

## Controllers (`adapter/in/web/`)

- Depend on inbound port interfaces, NOT use case classes directly
- Responsibilities: request validation (via DTOs), calling the port/in, JWT generation (auth endpoints), mapping domain objects to response DTOs
- **`request.toCommand()`** — request DTOs expose this method; controllers call it to produce the command (no separate request→command mapper)
- **`{Entity}ResponseMapper`** in `adapter/in/web/{context}/mapper/` — controllers call `{Entity}ResponseMapper.toResponse(domainObject)`
- **Swagger annotations required**: `@Tag` on class, `@Operation` + `@ApiResponse` on each endpoint (include error responses with `ErrorResponse` schema)
- **Prefer `@ResponseStatus` over `ResponseEntity`** — return DTOs directly

## Outbound Adapters (`adapter/out/persistence/`)

JPA repositories implement domain outbound port interfaces.

## Configuration (`configuration/`)

Wires use cases to their port/in interfaces via `@Bean` methods.
