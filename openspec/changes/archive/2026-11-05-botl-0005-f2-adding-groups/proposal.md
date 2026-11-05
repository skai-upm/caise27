# Proposal

## Why

Milestone F2-C02 requires user groups: created with an identifier and name (possibly empty), with existing users added or removed while the group is modifiable, never containing the same user twice, and deletable only when no active group ticket exists. Group tickets (a later Delivery 2 milestone) depend on groups.

## What Changes

- Add groups managed by central park management, with commands:
  - `GROUP CREATE;Identifier;Name` — unique identifier, non-empty name, created empty.
  - `GROUP ADD;Identifier;Email` — adds an existing user; rejects duplicates.
  - `GROUP REMOVE;Identifier;Email` — removes a member.
  - `GROUP DELETE;Identifier` — deletes the group.
- Define the group's locked state as *derived*: a group is locked when at least one associated group ticket has a date equal to or later than the current system date. Locked groups reject `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE`; past tickets never lock. The state is never stored.
- Add narrow lookups in central management for "user exists" and "dates of a group's tickets". User management and group tickets are not in the code base yet (later in the batch), so both default to empty: until users exist, `GROUP ADD` rejects every email as unknown.
- `HELP` lists the GROUP commands.

### In scope
- Group lifecycle and membership rules, derived lock state.

### Out of scope
- Creating/deleting group tickets (`GROUPTICKET ...`), user registration, persistence of groups.

### Open questions
- Whether e-mail matching is case-insensitive is not stated; emails are compared exactly (after trimming).

## Capabilities

### New Capabilities
- `user-groups`: Creation, membership management and deletion of groups of registered users, including the derived lock rule based on active group tickets.

### Modified Capabilities
<!-- None. park-management requirements unchanged (central management already "centralizes" park elements). -->

## Impact

- `ParkController` gains group storage and lookups; new `Group`, `GroupCommands`; `HELP` text; tests.
