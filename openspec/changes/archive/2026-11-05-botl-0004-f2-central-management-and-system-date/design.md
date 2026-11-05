# Design

## Context

Commands are dispatched by keyword (text before the first `;`) in the shared `CommandProcessor`; keywords may contain spaces (`SYSTEM DATE`). See proposal.md - Why.

## Goals / Non-Goals

**Goals:** guarantee one central instance regardless of where it is requested; keep date/price changes validated and atomic.

**Non-Goals:** dependency-injection frameworks; storing users/tickets yet.

## Decisions

- **Singleton `ParkController`** (private constructor, eagerly initialised `private static final` instance, `getInstance()`): simplest JVM-guaranteed single instance, thread-safe without locking. Alternative `enum` singleton is equally safe but awkward to extend with state later; DI alone cannot guarantee a single instance "regardless of where access is requested".
- **State types**: `LocalDate` for the date, `BigDecimal` (scale 2) for money — avoids binary floating-point rounding for prices.
- **Parsing without regular expressions** (Delivery 1 restriction kept): `LocalDate.parse` (ISO, strict resolver) for dates, plus a length check of 10; prices validated character by character (digits, one optional `.`, ≤2 decimals) before `new BigDecimal`.
- **`SystemCommands`** registers `SYSTEM DATE` and `SYSTEM PRICE` handlers in the processor; `App.run` wires it. Messages: `SYSTEM DATE UPDATED: <date>`, `SYSTEM PRICE UPDATED: <price>`, errors prefixed `ERROR:`.
- **`reset()`** on the controller restores the start-of-execution state; used by `App.run` at start (a fresh execution) and by tests.

## Risks / Trade-offs

- [Global mutable singleton complicates tests] → `reset()` restores initial state before each test.
