# Design

## Context

`App.main` builds a file-backed `UserRepository` and a `CommandProcessor(UserRepository)`. Tests construct `CommandProcessor` with in-memory repositories. Source §3 requires a single central object "regardless of where in the application access … is requested", so uniqueness must be guaranteed by the design, not by convention.

## Goals / Non-Goals

**Goals:**
- One `ParkController` instance per JVM execution, holding users, date and price.

**Non-Goals:**
- Dependency injection or replacing the pattern. A later milestone (Delivery 3, "singleton and dependency injection") revisits how the instance is obtained.
- Persisting the date or price.

## Decisions

- **Singleton via the initialization-on-demand holder idiom**: a private constructor and `static ParkController getInstance()`. This is thread-safe and lazy, and it is the simplest construction that makes a second instance impossible from outside the class.
- **Execution state reset via `startExecution(UserRepository users)`**: sets the users store, `date = LocalDate.now()` and `price = 40.00`. `App` calls it once. `CommandProcessor(UserRepository)` also calls it so that each test starts from a clean state. The instance itself is never replaced, so uniqueness holds.
- **`BigDecimal` for money**, scale 2, to avoid binary rounding. Validation is done without regex: only digits and at most one `.`, at most two decimals, and a value greater than 0. `1E2` and signs are therefore rejected.
- **Date parsing** with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`, which rejects `2027-02-30` and non-padded forms.
- **Dispatch on the first element**: `SYSTEM DATE` and `SYSTEM PRICE` are both added as command names in the existing `switch`.

## Risks / Trade-offs

- [Global mutable singleton complicates test isolation] → `startExecution` resets all state. Tests run sequentially in one fork.
