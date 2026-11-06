# Proposal

## Why

Milestone F2-C01 (Delivery 2, step 1) introduces the element that maintains and coordinates the overall park state — a single central management object — together with the current system date and the current base ticket price, and the `SYSTEM DATE` and `SYSTEM PRICE` commands that change them. Later Delivery 2 milestones (groups, tickets, attractions) depend on this central state.

## What Changes

- Add a central park management object of which at most one instance exists per execution, guaranteed wherever access is requested; it centralizes access to the users (and, in later milestones, groups, tickets and attractions).
- Keep a current system date, initially the local date when execution starts, replaceable with `SYSTEM DATE;<yyyy-MM-dd>` any number of times.
- Keep a current base ticket price, initially 40.00 euros, replaceable with `SYSTEM PRICE;<Price>` (valid, positive monetary amount); it lasts only for the current execution.
- List both new commands in `HELP`.

### In scope
- Source sections 3, 4.1 and 4.2 (the price's effect on tickets is applied when tickets exist, in later milestones).

### Out of scope
- Groups, tickets, attractions and their persistence (F2-C02 onwards).

## Capabilities

### New Capabilities
- `park-management`: the single central park management object, the current system date and the current base ticket price, and the `SYSTEM DATE`/`SYSTEM PRICE` commands.

### Modified Capabilities
- `execution-control`: `HELP` also lists `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<price>`.

## Impact

- New `es.upm.park.controller.ParkController`; `CommandProcessor` obtains services and system state through it; `Main` starts it for each execution; tests updated.
