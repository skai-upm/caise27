# Design

## Context

`CommandProcessor` is the single object through which every command (from standard input or a file) is dispatched; it is created once per execution by `App.run`. No time-dependent rule exists yet. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** one execution-wide date with a single source of truth, settable by command, easy to pass to future business services.

**Non-Goals:** Singleton or global static state; persistence of the date; a command to print the date (not requested; can be added later if needed).

## Decisions

- **The main controller is `CommandProcessor`.** It gains a `LocalDate currentDate` field plus `getCurrentDate()`. A second constructor `CommandProcessor(UserService, LocalDate initialDate)` lets tests fix the start date; the default constructor uses `LocalDate.now()`. That is the only place reading the real clock. Alternative — a static/global clock or Singleton — rejected: the source explicitly does not require it and one instance per execution already guarantees uniqueness.
- **How later rules get the date:** future services will receive the date from the controller (passed as an argument or via a `Supplier<LocalDate>` bound to `getCurrentDate`), never via `LocalDate.now()`.
- **Parsing:** `SYSTEM DATE` is recognised like `USER ...` (first token split on a space: `SYSTEM` + `DATE`) and requires exactly one parameter. The value is parsed with `DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)`, so the format is exact (two-digit month/day) and impossible dates such as 2026-02-30 are rejected. On `DateTimeParseException` the field is untouched.
- **Responses:** `OK: System date set to 2026-09-15`; `ERROR: Invalid date <value>. Expected format yyyy-MM-dd`.
- `HELP` gets `SYSTEM DATE;yyyy-MM-dd` added to its constant list.

## Risks / Trade-offs

- [Date lost on restart] → required by the source ("need not be persisted").
- [Open question] Whether a query form (e.g. `SYSTEM DATE` without a value) should print the current date is not specified; it is treated as missing parameter (error) to keep the smallest behaviour.
