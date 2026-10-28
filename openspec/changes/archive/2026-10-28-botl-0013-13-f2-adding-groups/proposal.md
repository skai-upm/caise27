# Proposal

## Why

Milestone F2-C02: registered users must be organizable into groups (later used for group tickets). Groups are created with an identifier and a name, possibly empty, and users are added or removed while the group is modifiable; a user can appear only once per group.

## What Changes

- Add groups with a unique identifier, a name and a set of members who are existing users.
- Add commands `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, `GROUP DELETE;Identifier`.
- Enforce: duplicate group identifiers rejected; only existing users can be added; a user already in the group is rejected; removing a non-member is rejected.
- Derive the group's locked state (never stored) from its group tickets and the current system date: `GROUP ADD`/`GROUP REMOVE` are rejected while the group has an active group ticket (date ≥ current date) and `GROUP DELETE` is rejected under the same condition. Group tickets arrive in F2-C04; until then no group has tickets, so no group is locked, but the rule is wired through a single derived check.
- Groups are owned by the park controller; `HELP` lists the group commands.
- Commit with the mandatory message `[F2-C02] Add user groups`.

## Capabilities

### New Capabilities
- `user-groups`: Groups of users — creation, membership changes, deletion, uniqueness rules and the derived lock based on active group tickets.

### Modified Capabilities
- `execution-control`: `HELP` also lists the four group commands.
- `user-deletion`: deleting a user also removes it from every group, so groups only contain existing users.

## Impact

- New `model.Group`, `repository.GroupRepository`, `service.GroupService`; `ParkController` exposes groups; `CommandProcessor` handles `GROUP *`. `UserOperationException` is generalized/renamed to `OperationException` (shared by all operations).
- Groups are not persisted yet (complete park persistence is F2-C08).
- Ambiguities flagged:
  - Members are tracked by DNI (immutable) so they remain the same user after `USER UPDATE` changes their email.
  - The statement does not say what happens to group membership when a user is deleted; to keep "groups consist of users who exist in the system", the user is removed from all groups.
  - Group identifiers are compared exactly (case-sensitive); identifier and name must be non-empty.
  - Responses: `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`, `GROUP DELETED`.
