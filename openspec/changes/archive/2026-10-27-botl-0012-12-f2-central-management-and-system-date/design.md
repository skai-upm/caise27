# Design

## Context

`Main` builds a `UserRepository` and a `CommandProcessor` per run. Delivery 2 adds groups, tickets and attractions that relate to each other, so one coordinating object is required (source §3).

## Goals / Non-Goals

**Goals:** a guaranteed single controller; date and price state; SYSTEM commands.
**Non-Goals:** groups/tickets/attractions (later milestones), persistence of date/price (explicitly not persisted).

## Decisions

- **Classic lazy Singleton** `ParkController.getInstance()` with a private constructor. Single-threaded application, so a simple lazily initialised static field suffices.
- **`start(UserRepository users)`** re-initialises the per-execution state (users source, date = `LocalDate.now()`, price = 40.00). `Main.run` calls it once at the beginning of each execution; tests calling `Main.run` repeatedly thus simulate fresh executions without creating new instances.
- **`BigDecimal` with scale 2** for money, avoiding binary floating-point rounding.
- **Strict parsing**: `LocalDate.parse` (ISO `yyyy-MM-dd`, rejects impossible dates) guarded by a length-10 check; price parsed by scanning characters (digits and one optional `.` with ≤2 decimals) before `new BigDecimal`, so no regex is needed.
- **`CommandProcessor`** keeps one entry point but reads the users repository from the controller; its constructor now receives the controller.

## Risks / Trade-offs

- [Singleton state leaks between unit tests] → every test (and every execution) calls `start(...)` first.
