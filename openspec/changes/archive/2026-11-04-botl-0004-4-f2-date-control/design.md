# Design

## Context

`CommandProcessor` is the single entry point that interprets every command (shared by interactive and file modes through `CommandRunner`); `App` wires it up. No current operation depends on dates. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One mutable current date per execution, owned by the controller and handed to later time-dependent operations.

**Non-Goals:**
- Singleton or global static state, persistence of the date, time-of-day, time zones, a command to print the date.

## Decisions

- **The controller is `CommandProcessor`.** It dispatches all commands, so it satisfies "stored in the application's main controller". It gets a `private LocalDate currentDate` and `public LocalDate getCurrentDate()`; later features receive the date from it (method argument or reference to the controller), never from `LocalDate.now()`. Alternative (separate `SystemDate` holder object) rejected for now: the source places the date in the controller and a holder would add a class with no other behavior.
- **Initialization is injected.** Constructor `CommandProcessor(UserManager, LocalDate initialDate)`; the existing one-argument constructor delegates with `LocalDate.now()`. `App` is the only place that reads the OS date (via that constructor), which keeps tests deterministic.
- **Strict parsing.** Value must have length 10 and parse with `DateTimeFormatter.ISO_LOCAL_DATE` (strict resolver), giving exactly `yyyy-MM-dd` and rejecting impossible dates such as `2026-02-30`. `DateTimeParseException` becomes an `ERROR:` result; the field is only assigned after a successful parse (atomic).
- **Command shape** follows existing parsing: operation words `SYSTEM` `DATE`, exactly one nonempty parameter.

## Risks / Trade-offs

- [Later features calling `LocalDate.now()` by habit] → spec requirement "Single current system date" plus code review; tests of later features set dates explicitly.

## Open Questions

- The source offers no command to display the current date; scenario "date inspected" is verified through the controller accessor in tests. A display command can be added later without changing this design.
