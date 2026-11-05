# Proposal

## Why

Milestone F2-C01 (Delivery 2, step 1) introduces the element that maintains and coordinates the overall park state: a single central object that gives access to the park's users (and, in later milestones, groups, tickets and attractions). It also holds the current system date and the current base ticket price, which date- and price-dependent operations in later milestones rely on. The commands `SYSTEM DATE` and `SYSTEM PRICE` must be available (source §3, §4.1, §4.2). Today the users store is passed directly to the command processor, and there is no notion of a system date or price.

## What Changes

- Add a central park management object that has exactly one instance per execution, whichever part of the application requests it. It centralizes access to the users store and holds the current system date and base ticket price.
- The system date starts as the local date when execution begins; the base price starts at 40.00 euros on every execution and is not persisted.
- Add `SYSTEM DATE;<yyyy-MM-dd>` to replace the current date with a valid calendar date. It may be used any number of times.
- Add `SYSTEM PRICE;<Price>` to replace the base price with a valid, positive monetary amount (at most two decimals). The new price applies to tickets created afterwards; tickets do not exist yet, so the observable effect now is the stored current price.
- Route command processing through the central object, and list both new commands in `HELP`.

In scope: the central object, date and price state, and the two commands.

Out of scope: groups, tickets and attractions (later Delivery 2 milestones), and persisting the date or price.

## Capabilities

### New Capabilities
- `park-management`: The single central park management object and the park-wide state it holds — current system date and base ticket price — plus the `SYSTEM DATE` and `SYSTEM PRICE` commands.

### Modified Capabilities
- None. `execution-control` requires `HELP` to list *at least* the Delivery 1 commands; adding the new commands to the listing keeps it satisfied. The other user capabilities are unchanged.

## Impact

- New class `park.ParkController`; `CommandProcessor` obtains users through it and handles the new commands; `App` initializes it; tests.
