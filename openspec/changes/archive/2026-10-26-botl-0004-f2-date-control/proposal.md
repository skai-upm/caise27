# Proposal

## Why

Upcoming Delivery 2 features (tickets, group locks, attraction access) depend on the current date. To execute and test those rules deterministically, the application needs one simulated system date that operators can move freely, instead of reading the computer's clock inside business rules.

## What Changes

- New command `SYSTEM DATE;yyyy-MM-dd` that sets the application's current date; the date may move backward or forward.
- Malformed or impossible dates are rejected and the current date is kept.
- A single current date is held by the main controller and shared by everything that needs time; at start-up it equals the operating system's date and is not persisted.
- `HELP` lists the new command.

## Capabilities

### New Capabilities
- `system-date`: The application's single, configurable current date used by every time-dependent rule.

### Modified Capabilities
- `command-interface`: `HELP` must also list `SYSTEM DATE;yyyy-MM-dd`.

## Impact

- New `SystemDate` holder; `CommandProcessor` (main controller) owns it and handles `SYSTEM DATE`; `App` wiring updated.
- Tests for valid/invalid dates, date changes and HELP output.
- No new dependencies; no persistence changes.
