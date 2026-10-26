# Proposal

## Why

Delivery 2 introduces time-dependent rules (ticket validity, deletion limits, attraction access). To execute and test them deterministically, the whole application needs one simulated current date that can be set by command, instead of each rule consulting the computer's clock.

## What Changes

- New command `SYSTEM DATE;yyyy-MM-dd` that sets the application's current date; it can move freely backward or forward.
- Dates in any other format, or impossible calendar dates, are rejected and the current date is unchanged.
- The current date is held once, in the application's main controller, and every time-dependent operation must obtain it from there. At startup it is initialised to the operating system's current date; it is not persisted.
- HELP lists the new command.
- No Singleton is required or introduced.

## Capabilities

### New Capabilities
- `system-date`: The single simulated current date of the application — its initial value, how it is changed with `SYSTEM DATE`, validation of the date format, and the rule that time-dependent behaviour uses it instead of the real clock.

### Modified Capabilities
<!-- None: HELP already lists every supported command, so no command-interface requirement changes. -->

## Impact

- New `MainController` class holding the current date; `CommandProcessor` handles `SYSTEM DATE` and exposes it in HELP; `App` creates the controller at startup.
- Tests for valid/invalid dates and the date being shared. No new dependencies.
