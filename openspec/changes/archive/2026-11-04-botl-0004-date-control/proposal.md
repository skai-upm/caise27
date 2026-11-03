# Proposal

## Why

Upcoming F2 features (tickets, groups, attractions) depend on dates. To execute and test them deterministically, the application needs one simulated current date for the whole execution, configurable by command, instead of each rule reading the computer clock.

## What Changes

- The application's main controller (`CommandProcessor`, the single object that dispatches every command) holds the one current system date for the execution.
- At startup that date is the operating system's local date; it is not persisted.
- New command `SYSTEM DATE;yyyy-MM-dd` (e.g. `SYSTEM DATE;2026-09-15`) sets the date; it can move backward or forward freely.
- A date in an incorrect format, or an impossible calendar date, is rejected and the current date stays unchanged.
- Date-dependent operations obtain the date from the controller; no business rule reads the real clock (none exist yet; this establishes the contract for later changes).
- `HELP` lists the new command.

## Capabilities

### New Capabilities
- `system-date`: The single execution-wide current date, its startup value, the `SYSTEM DATE` command and the rule that date-dependent behaviour uses it.

### Modified Capabilities
- None. (`application-control`'s HELP requirement already demands listing every available command; the new line follows from it.)

## Impact

- **Code**: `CommandProcessor` (date field, `getCurrentDate()`, `SYSTEM DATE` dispatch, HELP entry), tests.
- **Data**: none; the date is not persisted.
- **Out of scope**: Singleton pattern, persisting the date, tickets or any other future feature.
- **Open questions / assumptions**: the source defines no command to display the date and no response text — the response is `OK: System date set to <date>`; `SYSTEM DATE` without a parameter is rejected as a wrong parameter count; only the exact `yyyy-MM-dd` form (4-digit year, 2-digit month and day) of a real calendar date is accepted.
