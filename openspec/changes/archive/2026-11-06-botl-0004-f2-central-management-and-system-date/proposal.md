# Proposal

## Why

Milestone F2-C01 (Delivery 2, step 1) introduces the element that maintains and coordinates the overall park state. Later Delivery 2 features (groups, tickets, attractions, access) depend on a single shared park state, on a current date that can be changed for testing, and on a current base ticket price. None of these exist today: the application only has HELP and EXIT.

## What Changes

- Add a central park management component. Exactly one instance exists per execution, and every part of the application that requests it gets that same instance.
- The central component holds the current system date, initialized to the local date when execution starts, and the current base ticket price, initialized to 40.00 euros on every start.
- Add the command `SYSTEM DATE;<yyyy-MM-dd>`, which replaces the current system date. It can be used any number of times. An invalid date is rejected and leaves the date unchanged.
- Add the command `SYSTEM PRICE;<Price>`, which replaces the current base ticket price for the current execution only. The value must be a valid, positive monetary amount. Invalid values are rejected and leave the price unchanged.
- Extend HELP to list the two SYSTEM commands.

### In scope
- Central management singleton, system date, system base price, the two SYSTEM commands, HELP listing.

### Out of scope
- Users, groups, tickets and attractions held by the central component (their milestones add them). Ticket price snapshotting and group discounts belong to the ticket milestones. This change only provides the current base price they will read.
- Persisting date or price (the price explicitly resets every execution).

## Capabilities

### New Capabilities
- `park-management`: the single central park state and its system settings (current date, current base ticket price), together with the SYSTEM commands that change them.

### Modified Capabilities
- None. HELP's existing requirement already allows commands beyond its mandated minimum. The listing of SYSTEM commands is specified in `park-management`.

## Impact

- New `es.upm.park.controller.ParkController` (singleton).
- `es.upm.park.command.CommandProcessor`: dispatch on the command keyword, plus the SYSTEM DATE / SYSTEM PRICE handlers and HELP lines.
- New unit tests. No dependency or persistence changes.
