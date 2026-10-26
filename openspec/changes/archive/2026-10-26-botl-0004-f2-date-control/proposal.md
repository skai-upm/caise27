# Proposal

## Why

Delivery 2 introduces time-dependent rules (tickets valid for a date, deletions blocked by future tickets). They must be testable without depending on the computer's clock, so the application needs one simulated "current date" that commands can set freely.

## What Changes

- New command `SYSTEM DATE;yyyy-MM-dd` that sets the application's current date; invalid formats or impossible dates are rejected.
- A single current date shared by the whole application, held by the main controller and starting at the operating system's date when the application starts (not persisted).
- The date can be moved backward or forward at any time and the new value applies immediately to later commands.
- All future time-dependent rules must read this date instead of the real clock.
- HELP lists the new command.

## Capabilities

### New Capabilities
- `system-date`: the application's single simulated current date and the command that sets it.

### Modified Capabilities
<!-- None: HELP already lists every available command by requirement. -->

## Impact

- New `SystemDate` class; `CommandProcessor` and `App` wiring updated.
- No persistence or dependency changes.
