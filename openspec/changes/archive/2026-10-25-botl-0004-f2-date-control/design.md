# Design

## Context

`App.start` creates `UserManager` and `CommandProcessor`; `CommandProcessor` dispatches commands with a `switch` and keeps the `COMMAND_SYNTAX` list used by `HELP`. No component reads the date today.

## Goals / Non-Goals

**Goals:**
- A single mutable date object that later managers (tickets, groups, attractions) receive by reference.

**Non-Goals:**
- Singleton, persistence of the date, any time-dependent business rule (introduced by later proposals).

## Decisions

- **`SystemDate` holder class** with `LocalDate get()` and `void set(LocalDate)`, created once in `App.start` with `LocalDate.now()` and passed to `CommandProcessor` (the main controller, which owns and exposes it through `getSystemDate()`). Later managers are constructed with the same instance. Alternative: a `LocalDate` field in `CommandProcessor` plus a `Supplier<LocalDate>` for each manager — rejected because the holder makes the single shared instance explicit and avoids circular references between the controller and managers.
- **Only `App` calls `LocalDate.now()`**; tests construct `SystemDate` with fixed dates, so no rule depends on the real clock.
- **Parsing** with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`, which rejects non-padded fields and impossible dates such as `2026-02-30`. A static `SystemDate.parse(String)` returns `null` on failure so the processor can answer `ERROR INVALID DATE`. `String.split` is still used for the command itself.
- **Response** `OK SYSTEM DATE -> <date>` follows the `OK … -> <value>` style used by later Delivery 2 commands.

## Risks / Trade-offs

- [A future component could call `LocalDate.now()` directly] → Convention documented here and in the `system-date` spec; reviews and tests use fixed dates.
