# Design

## Context

`App` builds `UserFileStorage` → `UserRepository` (loaded) → `UserService` → `CommandProcessor`. Delivery 2 adds park-wide state. See proposal.md.

## Goals / Non-Goals

**Goals:** a single, globally reachable controller owning the park state; date and price state with commands.
**Non-Goals:** groups/tickets/attractions (later milestones); persisting date or price.

## Decisions

- **`park.ParkController` as an eager singleton** (`private` constructor, `static final INSTANCE`, `getInstance()`), satisfying "at most one instance regardless of where it is requested". Alternative: create one in `App` and pass it around — does not *guarantee* uniqueness.
- **`initialize(UserStorage)`** (re)sets the controller's state for a new execution: loads users, sets date to `LocalDate.now()` and price to 40.00. `App` calls it once at startup; tests call it for a fresh state. It resets state, never creates instances.
- **Controller API**: `users()` → `UserService`; `getCurrentDate()/setCurrentDate(LocalDate)`; `getBasePrice()/setBasePrice(BigDecimal)`. Money as `BigDecimal` with scale 2 to avoid floating-point error.
- **Parsing in the CLI layer**: date via `DateTimeFormatter.ofPattern("uuuu-MM-dd")` with `ResolverStyle.STRICT`; price via a char-by-char check (digits, optional single `.`, ≤2 decimals) then `new BigDecimal`, > 0 — no regular expressions.
- `CommandProcessor` takes the `ParkController`; the `UserService` used for user commands comes from it.

## Risks / Trade-offs

- [Singleton state shared across tests] → tests call `initialize` in setup; Surefire runs test classes sequentially in one JVM by default.
