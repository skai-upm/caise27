# Proposal

## Why

Delivery 2 (milestone F2-C01) relates users with groups, tickets and attractions and makes operations depend on a current date and price. The application needs one central element that owns the park state — including the current system date (initially today's local date) and the current base ticket price (initially 40.00 €) — and the `SYSTEM DATE` and `SYSTEM PRICE` commands to change them during an execution.

## What Changes

- Add a park controller: the single object holding the overall park state during an execution and giving access to user management (later also groups, tickets and attractions). Its design guarantees at most one instance per execution regardless of where it is accessed.
- Keep a current system date, initialized to the local date at startup, replaceable with `SYSTEM DATE;<yyyy-MM-dd>` any number of times.
- Keep a current base ticket price, initialized to 40.00 € at every startup, replaceable with `SYSTEM PRICE;<Price>` (valid positive monetary amount); not persisted.
- Route command processing through the park controller; extend `HELP` with both new commands.
- Commit with the mandatory message `[F2-C01] Add park controller and system date`.

## Capabilities

### New Capabilities
- `park-management`: The single central park controller, the current system date and the current base ticket price, with the `SYSTEM DATE` and `SYSTEM PRICE` commands.

### Modified Capabilities
- `execution-control`: `HELP` must also list `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.

## Impact

- New `park.ParkController` (singleton); `App` initializes it at startup and `CommandProcessor` uses it.
- Ticket creation (which uses the price "from that moment onward", and group discounts on it) arrives in F2-C03/F2-C04; this change only stores the price.
- Ambiguities flagged:
  - "Valid, positive monetary amount" is interpreted as a plain decimal number greater than zero with at most two decimal places and `.` as separator (e.g. `45.50`, `45`); signs, exponents, commas or more decimals are rejected.
  - Dates must be valid calendar dates in exactly `yyyy-MM-dd` form (e.g. `2027-02-30` is rejected).
  - Responses: `SYSTEM DATE UPDATED` and `SYSTEM PRICE UPDATED`; errors as `ERROR: <reason>`.
