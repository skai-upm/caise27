# Proposal

## Why

Upcoming F2 features (tickets, validity periods, visits) depend on the current date. To execute and test them reproducibly, the application needs one simulated date that can be moved freely instead of reading the computer clock.

## What Changes

- New command `SYSTEM DATE;yyyy-MM-dd` that sets the application's current date; incorrectly formatted or non-existent dates are rejected.
- A single current date, held by the main controller, shared by every operation; it can move backward or forward at any time and the change applies immediately to subsequent commands.
- Each execution starts with the operating system's current date; the simulated date is not persisted.
- Rule for all present and future time-dependent logic: use the system date from the controller, never the computer's real date directly.
- HELP lists the new command.

## Capabilities

### New Capabilities
- `system-date`: The application-wide simulated current date, how it is initialised and changed, and its use by time-dependent rules.

### Modified Capabilities
- `command-processing`: HELP must also list `SYSTEM DATE;yyyy-MM-dd`.

## Impact

- `CommandProcessor` (the main controller) stores the current date and handles `SYSTEM DATE`; HELP text updated.
- New tests for date parsing and command handling.
- No persistence or user-model changes.
