# Proposal

## Why

Milestone F2-C01 (Delivery 2) needs one element that maintains and coordinates the overall park state — users now, and groups, tickets and attractions in the following milestones — together with a current system date and a current base ticket price on which date- and price-dependent operations will rely. Today users are reached only through objects wired in `App`, and the application has no notion of date or price.

## What Changes

- Add a central park management object, guaranteed to have a single instance per execution regardless of where it is requested; it gives access to the users repository (and will host groups, tickets and attractions).
- Add a current system date, initialised to the local date at start-up, changeable with `SYSTEM DATE;<yyyy-MM-dd>` any number of times.
- Add a current base ticket price, 40.00 euros at the start of each execution, changeable with `SYSTEM PRICE;<Price>` (valid positive monetary amount); not persisted.
- Extend `HELP` with the two new commands.
- Out of scope: groups, tickets, attractions (F2-C02+); persistence of park state (F2-C08).

## Capabilities

### New Capabilities
- `park-management`: The single central park manager, the current system date and the current base ticket price, with the SYSTEM DATE and SYSTEM PRICE commands.

### Modified Capabilities
<!-- None: execution-control's HELP requirement lists a minimum set, which still holds; the help text is extended without changing its requirements. -->

## Impact

- New `park.Park` singleton; `App` initialises it at start-up; `CommandProcessor` handles `SYSTEM DATE`/`SYSTEM PRICE`.
