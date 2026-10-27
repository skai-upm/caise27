# Proposal

## Why

Milestone F2-C01 opens Delivery 2: the park needs one central element that owns the overall state (users now; groups, tickets and attractions in later milestones), the current system date and the current base ticket price. Date-dependent operations must be testable, so the date must be settable at run time with `SYSTEM DATE`, and the price with `SYSTEM PRICE`.

## What Changes

- Add a single central park controller; the design guarantees at most one instance per execution regardless of where it is requested.
- The controller centralises access to users (the existing user repository) and will later own groups, tickets and attractions.
- Keep a current system date, initialised to the local date at start-up, replaceable with `SYSTEM DATE;<yyyy-MM-dd>` any number of times.
- Keep a current base ticket price, initialised to 40.00 euros at every start-up, replaceable with `SYSTEM PRICE;<Price>` (valid, positive monetary amount); not persisted between executions.
- Commit with the exact message `[F2-C01] Add park controller and system date`.

## Capabilities

### New Capabilities
- `park-management`: The single central park controller and the system date and base price it maintains, with the SYSTEM DATE and SYSTEM PRICE commands.

### Modified Capabilities
<!-- none -->

## Impact

- New `park/ParkController`; `CommandProcessor` obtains users through it; `Main` initialises it at start-up.
- Ambiguities flagged: responses are `SYSTEM DATE UPDATED` / `SYSTEM PRICE UPDATED` (wording is free); a valid monetary amount is written with digits and an optional `.` followed by at most two decimals (e.g. `45`, `45.5`, `45.50`); signs, exponents, commas and zero are rejected; a date must be a real calendar date in exactly `yyyy-MM-dd` form. "Price will not alter tickets already created" applies once tickets exist (F2-C03).
