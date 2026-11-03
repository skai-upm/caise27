# Proposal

## Why

Upcoming F2 features (tickets, discounts, access) depend on dates. To execute and test them deterministically, the application needs one simulated current date, configurable by command, instead of reading the computer clock wherever a date is needed.

## What Changes

- The application's main controller (`CommandProcessor`) holds one current date for the whole execution, initialised to the operating system's local date at startup.
- New command `SYSTEM DATE;yyyy-MM-dd` (e.g. `SYSTEM DATE;2026-09-15`) sets the current date; the date may move backward or forward freely.
- A date with an incorrect format or a non-existent calendar date is rejected and the current date stays unchanged.
- The current date is exposed to business logic through the controller so every later time-dependent rule uses it; no business rule reads the real clock.
- The date is not persisted; each execution starts from the OS date.
- `HELP` lists the new command.
- No Singleton, no tickets or other future features.

## Capabilities

### New Capabilities
- `system-date`: the single, execution-wide simulated current date and the `SYSTEM DATE` command.

### Modified Capabilities
- `application-control`: `HELP` must also show `SYSTEM DATE;yyyy-MM-dd`.

## Impact

- `CommandProcessor` (date field, command, help text); `App` unchanged except for wiring if needed.
- No change to user model, validation, persistence or file formats.
