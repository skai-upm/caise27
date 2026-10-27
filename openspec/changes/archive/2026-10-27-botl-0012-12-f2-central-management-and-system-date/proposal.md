# Proposal

## Why

Milestone F2-C01 starts Delivery 2 by adding a single central element that maintains and coordinates the park state — the current system date and the current base ticket price, and access to users (later groups, tickets and attractions). Date-dependent operations of later milestones need a replaceable system date, and ticket pricing needs a current base price.

## What Changes

- Add a central park controller with exactly one instance per execution, reachable from anywhere in the application.
- The system date starts as the local date when execution begins; `SYSTEM DATE;<yyyy-MM-dd>` replaces it, any number of times.
- The base ticket price starts at 40.00 euros each execution; `SYSTEM PRICE;<Price>` replaces it with a valid positive monetary amount for the rest of the execution only.
- HELP lists `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.
- Record the mandatory milestone commit `[F2-C01] Add park controller and system date` once verified.

## Capabilities

### New Capabilities
- `park-management`: The central park state for one execution — the single controller, the current system date and the current base ticket price.

### Modified Capabilities
- `command-processing`: HELP additionally lists the SYSTEM commands.

## Impact

- New `ParkController` singleton; `App` initialises it at startup; `CommandProcessor` handles `SYSTEM DATE` and `SYSTEM PRICE`.
- Ticket creation using the price arrives with F2-C03/F2-C04 ("price of tickets already created" is out of scope until tickets exist).
