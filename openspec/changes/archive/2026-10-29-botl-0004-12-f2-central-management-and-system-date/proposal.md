# Proposal

## Why

Milestone F2-C01 (Delivery 2) requires a single element that maintains and coordinates the overall park state, including the current system date and the current base ticket price, plus the `SYSTEM DATE` and `SYSTEM PRICE` commands so date- and price-dependent operations can be tested deterministically.

## What Changes

- Add a central park-management element of which at most one instance can exist during an execution, regardless of where it is accessed from.
- It holds the current system date (initially the local date when execution starts) and the current base ticket price (initially 40.00 euros).
- New command `SYSTEM DATE;<yyyy-MM-dd>` replaces the current system date; it may be used any number of times in an execution.
- New command `SYSTEM PRICE;<Price>` replaces the current base ticket price with a valid, positive monetary amount; the value only affects the current execution.
- Invalid dates, invalid/non-positive prices and wrong argument counts are rejected with an error message and leave the state unchanged.
- HELP lists the two new commands.

## Capabilities

### New Capabilities
- `park-management`: the unique central park state (system date, base ticket price) and the SYSTEM commands that change it.

### Modified Capabilities
- `command-interface`: HELP must also list `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.

## Impact

- New class `ParkController` (singleton) and command dispatch additions in `CommandProcessor`; tests.
- No new dependencies.

## Ambiguities and unmet prerequisites (flagged)

- The source says central management centralises access to users, groups, tickets and attractions. None of these exist yet in this batch (their proposals come later or are delivered by other milestones), so this change only provides the date and price state; the controller is designed to receive those collections later.
- Effects of the price on tickets ("used as the base price for new tickets", group discounts, tickets already created keep their price) depend on ticket milestones not yet applied; here the requirement is limited to storing and exposing the current price.
- "Valid, positive monetary amount" is not further defined. This change accepts a plain decimal number using `.` as decimal separator, with at most two decimal places, strictly greater than zero (e.g. `45.50`, `45`, `0.01`); it rejects signs, exponents, commas and more than two decimals.
- Date format `yyyy-MM-dd` is interpreted strictly (four-digit year, two-digit month and day, real calendar date).
- Confirmation message text is not specified; `SYSTEM DATE UPDATED` / `SYSTEM PRICE UPDATED` are used, in the style of the Delivery 1 examples.
