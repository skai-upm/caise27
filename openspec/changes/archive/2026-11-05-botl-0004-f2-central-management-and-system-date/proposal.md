# Proposal

## Why

Milestone F2-C01 (Delivery 2, step 1) introduces the single element that maintains and coordinates the overall park state — including the current system date and the current base ticket price — and the `SYSTEM DATE` / `SYSTEM PRICE` commands to change them during an execution. Date-dependent and price-dependent features of Delivery 2 (tickets, access) need this shared, test-controllable state.

## What Changes

- Add a central park management object with exactly one instance per execution, reachable from anywhere in the application.
- It holds the current system date (initially the local date when execution starts) and the current base ticket price (initially 40.00 euros, reset on every new execution).
- Add `SYSTEM DATE;<yyyy-MM-dd>`: replaces the current system date; may be used any number of times.
- Add `SYSTEM PRICE;<Price>`: replaces the base price with a valid positive monetary amount.
- Invalid arguments are rejected with an error message and leave the state unchanged.
- `HELP` additionally lists the two new commands.

### In scope
- The central management element (date, price, single instance) and the two SYSTEM commands.

### Out of scope
- Users/groups/tickets/attractions held by central management (later milestones add them), ticket pricing and group discounts (the price is only stored here), persistence of the price (explicitly non-persistent).

### Assumptions
- "Valid monetary amount" is interpreted as a plain decimal number with at most two decimal places (e.g. `45.50`, `45`), strictly greater than zero.

## Capabilities

### New Capabilities
- `park-management`: Central, single-instance management of the overall park state, including the current system date and base ticket price and the commands that change them.

### Modified Capabilities
<!-- None. HELP's requirement already says "at least"; listing extra commands does not change it. -->

## Impact

- New `ParkController` (central management) and SYSTEM command handlers; `App` wires them into the shared `CommandProcessor`.
- `HELP` text gains two lines. No persistence or dependency changes.
