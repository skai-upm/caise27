# Proposal

## Why

Delivery 2 introduces time-dependent rules (tickets valid on a date, deletion allowed only for future tickets, attraction access on the current day). To execute and test them deterministically, the application needs a single simulated current date that can be set by command, instead of each operation consulting the computer clock.

## What Changes

- New command `SYSTEM DATE;yyyy-MM-dd` that sets the application's current date; it can move freely backwards or forwards.
- Dates in an incorrect format, or impossible dates such as `2026-02-30`, are rejected and the current date is kept.
- One current date for the whole application, held by the main controller and shared by reference with every component that needs it; at startup it is the operating system's current date and it is not persisted.
- `HELP` lists the new command.
- No time-dependent business rule exists yet; future ones must read this date and never the real clock. Singleton is not required.

## Capabilities

### New Capabilities
- `system-date`: The simulated current date of the application: its initial value, how it is changed with `SYSTEM DATE`, validation, and the rule that time-dependent operations use it.

### Modified Capabilities
- `command-processing`: the `HELP` requirement lists `SYSTEM DATE;yyyy-MM-dd` among the available commands.

## Impact

- New class `SystemDate`; `CommandProcessor` (new command, owns the date) and `App` (creates it at startup).
- New tests for the command and the date holder.
