# Design

## Context

`App.run` wires `UserRepository` → `UserService` → `CommandProcessor`. Delivery 2 adds several related element collections that must be coordinated by one object.

## Goals / Non-Goals

**Goals:** a single park manager that later milestones extend; date/price state usable by tickets. **Non-Goals:** dependency injection framework (none allowed); persisting date/price.

## Decisions

- **Singleton via private constructor + static holder (`Park.getInstance()`)**: guarantees one instance per JVM wherever it is requested, lazily and thread-safely. Alternative (enum singleton) works too, but a class keeps future evolution (F3 singleton/DI milestone) simpler.
- **`Park.start(UserRepository)`** resets the execution state (date = `LocalDate.now()`, price = 40.00) and installs the users repository; called once by `App.run` at start-up (and by tests to isolate executions). Initial state without `start` uses an in-memory repository.
- **`BigDecimal` for prices** (scale 2) to avoid floating-point rounding in later discount calculations. Validation: only digits and at most one `.`, at most two decimals, value > 0 — checked by character scanning (no regex).
- **Strict date parsing** with `DateTimeFormatter.ofPattern("uuuu-MM-dd")` and `ResolverStyle.STRICT` so `2027-02-30` is rejected.
- **Commands in `CommandProcessor`** delegating to `Park` (`setCurrentDate`, `setCurrentPrice`).

## Risks / Trade-offs

- [Global state makes tests interdependent] → tests call `Park.getInstance().start(...)` in setup.
