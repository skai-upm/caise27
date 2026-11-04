# Design

## Context

`CommandProcessor` is the application's main controller: both input modes funnel every command through it, and it owns the run-wide state (`exitRequested`). There is no time-dependent business rule yet; tickets and attraction access come in later changes.

## Goals / Non-Goals

**Goals:**
- One date per run, owned by the controller, readable by any later feature through the controller.
- Strict `yyyy-MM-dd` parsing with no partial updates.

**Non-Goals:**
- Singleton or global static state (the source explicitly does not require it).
- Persisting the date, adding a query command, or any date-dependent business rule.

## Decisions

- **Store the date as a `LocalDate` field in `CommandProcessor`.** The source says the date lives in the main controller. Because there is exactly one `CommandProcessor` per run (created in `App.run`), there is exactly one date. It is exposed via `getCurrentDate()`. Later services receive it from the controller (as a parameter or a supplier) instead of calling `LocalDate.now()`. Alternative considered: a separate `SystemDate` holder object passed around. Rejected for now because no consumer exists yet and the source places the date in the controller.
- **Startup value injectable for tests.** The existing `CommandProcessor(UserManager)` constructor delegates to a new `CommandProcessor(UserManager, LocalDate)` with `LocalDate.now()`. That is the only call to the real clock. Tests pass a fixed date.
- **Strict parsing.** `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)` accepts only four-digit years, two-digit months and days, and real calendar dates (rejects `2026-02-30`, `2026-9-15`). Parameters are trimmed like other commands. A `DateTimeParseException` becomes `ERROR: Invalid date ... expected yyyy-MM-dd`, and the field is assigned only after a successful parse.
- **Command token.** `SYSTEM DATE` is dispatched in the same `switch` as other commands. The existing operation normalisation (upper-case, single spaces) applies, so `system date;2026-09-15` also works, consistently with the other commands.
- **Output.** `OK: System date set to 2026-09-15`.

## Risks / Trade-offs

- [Date held in the controller couples later services to it] → they receive the value through parameters, which keeps them unit-testable.
- [Day changes at midnight during a long interactive run are not reflected] → intended: the date changes only via `SYSTEM DATE`.

## Open Questions

- The source mentions inspecting the date but defines no query command; none is added. Tests inspect it through `getCurrentDate()`, and the `SYSTEM DATE` result line echoes the new date.
