# Proposal

## Why

Upcoming F2 features (tickets, attraction access) depend on the current date. To execute and test them deterministically, the application needs one simulated, configurable date for the whole run instead of each rule consulting the computer clock.

## What Changes

- New command `SYSTEM DATE;yyyy-MM-dd` (e.g. `SYSTEM DATE;2026-09-15`) that sets the application's current date; it may move backward or forward freely.
- Invalid or malformed dates are rejected with `ERROR` and leave the current date unchanged.
- The current date lives in the main controller (`CommandProcessor`), is initialised to the operating-system local date at startup, and is the only date source for time-dependent business rules. It is not persisted.
- `HELP` lists the new command.
- No Singleton pattern, no tickets or other future features.

## Capabilities

### New Capabilities
- `system-date`: The execution-wide simulated current date, its initial value and the `SYSTEM DATE` command.

### Modified Capabilities
- `application-control`: "HELP command" now also lists `SYSTEM DATE;yyyy-MM-dd`.

## Impact

- `CommandProcessor` (date field, accessor, command, HELP list); `App` unchanged except for wiring (startup date).
- No current business rule uses dates yet; the change establishes the single source for later rules.
- Tests: `CommandProcessorTest` additions; `mvn test` regression.
