# Proposal

## Why

Milestone F2-C01 of Delivery 2 requires a single central element that maintains and coordinates the overall park state, including the current system date (initially the local date at start-up, replaceable during execution for testing) and the current base ticket price (initially 40.00 euros, modifiable per execution). Later Delivery 2 features (groups, tickets, attractions) depend on this central state and on the `SYSTEM DATE` and `SYSTEM PRICE` commands.

## What Changes

- Add a park controller: exactly one instance per execution, reachable from anywhere in the application, holding the current system date and current base ticket price.
- Add `SYSTEM DATE;<yyyy-MM-dd>` to replace the current system date (any number of times per execution).
- Add `SYSTEM PRICE;<Price>` to replace the current base price with a valid, positive monetary amount.
- Extend `HELP` with the two new commands.
- Commit with the exact message `[F2-C01] Add park controller and system date`.

## Capabilities

### New Capabilities
- `park-management`: Central park state (single park controller), current system date and current base ticket price with the SYSTEM commands that change them.

### Modified Capabilities
- `execution-control`: The HELP listing additionally shows `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.

## Impact

- New controller and SYSTEM command classes, HELP text update, unit tests.
- Users, groups, tickets and attractions are not yet present (later milestones); the controller will centralize them as they are added.
- **Flagged ambiguities:**
  - "Valid, positive monetary amount" is not defined further. Accepted form: digits with an optional `.` followed by at most two decimals (e.g. `45`, `45.5`, `45.50`); zero, negatives, other separators, exponents and more than two decimals are rejected.
  - Output texts for successful/rejected SYSTEM commands are not specified (source §16 allows message variations); messages follow the existing `ERROR: ...` convention.
  - The rules that the price applies to new tickets only and that group discounts use the price at purchase time belong to ticket milestones; this change only stores the price.
