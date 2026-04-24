# Testing

Run after every feature implementation to validate non-regression:

```bash
./mvnw test -B                                                        # unit tests
./mvnw test -B -Dtest='com.elo.acceptance.CucumberTest'              # acceptance tests
```

## Levels

| Level | Scope | Tools |
|---|---|---|
| Unit | Use cases, ELO algorithm, value objects | JUnit 5, Mockito |
| Integration | Repository adapters, DB queries | Testcontainers, Spring Boot Test |
| API | REST controllers, request/response cycle | MockMvc/RestAssured, Testcontainers |
| Acceptance | End-to-end feature validation | Cucumber (Gherkin) + Spring Boot Test + Testcontainers |

- Domain and application layers tested in **pure isolation** (no Spring context)
- **Each class gets its own dedicated test class** — no bundling multiple concerns

## File Locations

- Domain models → `src/test/java/com/elo/domain/{context}/model/{Model}Test.java`
- Use cases → `src/test/java/com/elo/application/{context}/usecase/{Action}UseCaseTest.java`
- Commands → `src/test/java/com/elo/application/{context}/command/{Action}CommandTest.java`
- Mappers → `src/test/java/com/elo/application/{context}/mapper/{Entity}MapperTest.java`
- Test package mirrors source package exactly

## Acceptance Tests (Cucumber)

Feature files: `src/test/resources/features/{identity,group,activity,match,ranking}/`

### Scenario structure

```gherkin
Scenario: <description>
  Given <precondition — existing data or first request field if no precondition>
  And a <action> request with <field> "<value>"   # first request field (if precondition exists)
  And the <action> <field> is "<value>"           # each subsequent request field
  When I submit the <action> request
  Then I receive a <NNN> <Status> response
  And <additional assertions>
```

### Step rules

- **Before writing any step**, search all existing step files for the same (or conflicting) step text — duplicates and parameterized vs. literal conflicts both cause `AmbiguousStepDefinitionsException` at runtime
- Generic status assertions and shared setup steps live in `CommonSteps.java` — never redeclare them in feature step classes
- All step classes use `ScenarioContext` (`@ScenarioScope` bean) to share `response` and `authToken` — step classes must never hold `response` as an instance field
- Step classes hold `pending*` fields populated by `And` steps and consumed by `When`
- **Step text must be human-readable and business-focused** — never expose HTTP methods, URLs, or technical details in `.feature` files
- **Prefer controller-based steps for test data setup** — call REST controllers via existing Cucumber steps; only use direct DB manipulation when no controller exists yet

### CucumberHooks

`CucumberHooks.java` deletes all tables between scenarios via `@Before`. **Each time a new JPA repository is added, add `repository.deleteAll()` to this hook** — failing to do so causes cross-scenario data contamination.

### ELO algorithm tests

Require exhaustive coverage: 1v1, team, FFA, draws, cancellation revert.
