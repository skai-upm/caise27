# Design

## Context

`CommandProcessor` is the main controller: every command from both input modes goes through `process(line)`. No business rule currently depends on the date. See proposal.md.

## Goals / Non-Goals

**Goals:**
- One mutable current date owned by the controller, injectable for tests.

**Non-Goals:**
- Singleton, global static state, persistence of the date, a date-query command.

## Decisions

- **Storage:** a `LocalDate currentDate` field in `CommandProcessor`, exposed via `getCurrentDate()`. Constructors: `CommandProcessor(UserManager)` initialises from `LocalDate.now()` (the only clock read in the application); `CommandProcessor(UserManager, LocalDate)` allows tests and future wiring to inject a start date. Alternative of a `static` holder or Singleton rejected: the source says the date lives in the main controller and does not require a Singleton.
- **Propagation to future rules:** later services receive the date from the controller as a method argument when a date-dependent command is executed, so there is no second date source.
- **Parsing:** `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`; rejects wrong order, missing zero padding and impossible days (2026-02-30). The value is not trimmed (consistent with user commands).
- **Output:** `OK: system date set to 2026-09-15`.

## Risks / Trade-offs

- [No command to display the current date] → Not requested; the `OK` message echoes the configured date. Open question: whether a later delivery wants a query form.
