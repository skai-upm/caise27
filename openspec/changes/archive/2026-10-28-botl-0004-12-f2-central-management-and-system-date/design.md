# Design

## Context

The application has a `CommandProcessor` dispatching by the text before the first `;` (so `SYSTEM DATE` and `SYSTEM PRICE` are distinct keywords), `HELP`/`EXIT` commands, executable JAR and file mode. There is no domain or state yet. Delivery 1 restrictions (standard library, no regex) still apply.

## Goals / Non-Goals

**Goals:** single park controller with date and price; SYSTEM commands; HELP update.

**Non-Goals:** users, groups, tickets, attractions and their use of date/price (later milestones); dependency-injection refactoring (F3 milestone).

## Decisions

- **`ParkController`** in `es.upm.etsisi.amusementpark.controller`: eager `private static final ParkController INSTANCE`, private constructor, `getInstance()`. Eager initialization is thread-safe and guarantees one instance regardless of the access point. Alternative: enum singleton — equivalent, but a class keeps room for the F3 refactoring.
- **Date** as `java.time.LocalDate`, initialised with `LocalDate.now()`; **price** as `BigDecimal` with scale 2 (`40.00`) to avoid floating-point money errors.
- **Parsing without regex**: date via `LocalDate.parse` (ISO `yyyy-MM-dd`, rejects impossible dates such as 2027-02-30) plus a length-10 check to reject expanded-year forms; price via a character loop (digits, at most one `.`, ≤2 decimals) then `new BigDecimal(...)`, positive check.
- **Testing reset**: package-private `reset()` restores start-up state for unit tests in the same package; production code never calls it. Each JVM start creates a fresh instance, giving the per-execution price.
- **Messages**: `SYSTEM DATE UPDATED: <date>`, `SYSTEM PRICE UPDATED: <price>`, errors `ERROR: Invalid date ...` / `ERROR: Invalid price ...`.

## Risks / Trade-offs

- [Global mutable singleton complicates tests] → tests set explicit values or call `reset()`; F3 will introduce dependency injection.
