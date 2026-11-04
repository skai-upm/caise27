# Proposal

## Why

Upcoming features (tickets, attraction access) depend on the date. To run and test them reproducibly, the application needs one simulated current date that can be moved backward and forward by command, instead of each rule reading the computer clock.

## What Changes

- The application holds a single current date for the whole run, stored in the main controller and initialised to the operating system's local date at startup.
- New command `SYSTEM DATE;yyyy-MM-dd` sets that date (forward or backward) for the rest of the run.
- Dates not in the exact `yyyy-MM-dd` form, or not existing in the calendar, are rejected and leave the current date unchanged.
- The date is not persisted; every run starts from the real local date.
- `HELP` lists the new command.
- No ticket or other date-dependent business rule is added in this change. Existing commands and the users file are unchanged.

## Capabilities

### New Capabilities
- `system-date`: the single, run-wide simulated current date, its initial value, the `SYSTEM DATE` command and the rule that time-dependent logic uses it.

### Modified Capabilities
- `application-control`: the `HELP` list must now include `SYSTEM DATE;yyyy-MM-dd`.

## Impact

- `CommandProcessor` (the main controller) gains the current date, its getter and the `SYSTEM DATE` handler; `HELP_LINES` gains one entry.
- No change to user classes, persistence format or input modes. No new dependencies (`java.time` only).
