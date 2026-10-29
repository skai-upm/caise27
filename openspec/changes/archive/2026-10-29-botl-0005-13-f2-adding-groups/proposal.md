# Proposal

## Why

Milestone F2-C02 requires user groups: groups with a unique identifier and a name, possibly empty at creation, to which existing users can be added or removed while the group is modifiable, never containing the same user twice; groups can also be deleted.

## What Changes

- New commands `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, `GROUP DELETE;Identifier`.
- Groups are kept by central park management; identifiers are unique.
- GROUP ADD only accepts users that already exist in the system and rejects a user already in the group.
- GROUP ADD / GROUP REMOVE are only allowed while the group has no active group ticket, and GROUP DELETE only when no associated group ticket has a date equal to or later than the current system date. The locked state is computed from tickets and the current date, never stored.
- HELP lists the four GROUP commands.

## Capabilities

### New Capabilities
- `user-groups`: creation, membership management and deletion of user groups, including the ticket-derived lock rules.

### Modified Capabilities
- `command-interface`: HELP also lists the GROUP commands.

## Impact

- New classes `Group`, `User` (minimal), additions to `ParkController` and `CommandProcessor`; tests.

## Ambiguities and unmet prerequisites (flagged)

- **Users:** the user model and USER CREATE belong to proposals not yet applied in this batch. This change introduces only the minimum needed for "users who already exist in the system": a `User` identified by its email and a user directory in central management with lookup by email. No command registers users yet, so through the command interface GROUP ADD currently reports the user as not found; success paths are exercised by tests that register users programmatically.
- **Group tickets:** they belong to a later milestone. The lock rules are implemented as a computation over the group tickets known to central management for the current system date; since none can exist yet, every group is currently modifiable and deletable. The rules become effective once group tickets are added.
- Validation of identifier and name is not specified beyond uniqueness; blank identifiers, blank names and blank emails are rejected as unprocessable input.
- Identifiers and emails are compared exactly (case-sensitive), as the source gives no normalization rule.
- Confirmation message texts are not specified; `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP` and `GROUP DELETED` are used.
