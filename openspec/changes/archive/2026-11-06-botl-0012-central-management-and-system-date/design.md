# Design

## Context

See proposal.md – Why. Today `Main` builds `UserRepository → UserService → CommandProcessor → CommandRunner`. Delivery 2 will add groups, tickets and attractions that must be reachable from one coordinating object.

## Goals / Non-Goals

**Goals:**
- One instance per execution regardless of where access is requested.
- Date and price held centrally; per-execution reset.

**Non-Goals:**
- Persisting the date or price; dependency-injection refactoring (a later Delivery 3 milestone).

## Decisions

- **`ParkController` (package `es.upm.park.controller`)** with a private constructor and a static `getInstance()` returning a single eagerly created instance. This is the only way the "no more than one instance regardless of where access is requested" constraint can be enforced by the type itself; an instance passed around by hand could not prevent `new`.
- **Per-execution start**: `start(UserRepository users, LocalDate today)` resets date to `today`, price to `40.00` and builds the `UserService`. `Main` calls `start(repo, LocalDate.now())`. Because tests run several executions in one JVM, `start` is what defines "a new execution"; the instance stays the same.
- **Price** as `BigDecimal` with scale 2; parsing checks characters manually (digits, at most one `.`, at most two decimals, no sign/exponent) and requires `> 0`. No regex, consistent with Delivery 1 restrictions.
- **Date** parsed with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)` so `2027-02-30` fails.
- **`CommandProcessor(ParkController)`** replaces the `UserService` constructor; user commands use `park.getUserService()`. Messages: `System date set to 2027-04-15`, `System price set to 45.50`, errors `Error: invalid date <v>`, `Error: invalid price <v>`, `Error: SYSTEM DATE expects 1 fields`.

## Risks / Trade-offs

- [Global state shared between tests] → every test that needs it calls `start(...)` first; `start` fully resets the per-execution state.
