# Proposal

## Why

Upcoming time-dependent rules (tickets, validity periods) must be testable without depending on the computer's real date. The application needs one simulated current date for the whole execution, configurable by command and movable backward or forward.

## What Changes

- The application's main controller (the command processor that dispatches every command) holds the single current system date for the execution.
- At startup the current date is the operating system's local date; it is not persisted.
- New command `SYSTEM DATE;yyyy-MM-dd` (e.g. `SYSTEM DATE;2026-09-15`) sets the current date; any valid date, earlier or later, is accepted.
- A date with an incorrect format or a non-existent calendar date is rejected and the current date is unchanged.
- Every time-dependent operation must read this date; no business rule reads the computer's clock directly. (No time-dependent business rule exists yet.)
- `HELP` lists the new command.
- No Singleton pattern, no persistence of the date, no tickets or other future features.

The source requests the change name `configure-system-date`; this batch uses the BOTL ID `botl-0004-4-f2-date-control` for traceability.

## Capabilities

### New Capabilities
- `system-date`: The execution-wide simulated current date, its initialization and the `SYSTEM DATE` command.

### Modified Capabilities
- `application-control`: `HELP` must also list `SYSTEM DATE;yyyy-MM-dd`.

## Impact

- `CommandProcessor` gains the current-date state, an accessor for time-dependent operations, and the `SYSTEM DATE` command; its help list gains one line.
- `App` creates the controller with the startup local date.
- No change to user data, file formats or existing command syntax.
