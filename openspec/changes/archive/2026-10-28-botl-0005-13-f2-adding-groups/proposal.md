# Proposal

## Why

Milestone F2-C02 requires that registered users can be organised into groups: a group is created with a unique identifier and a name (possibly empty), users that already exist in the system are added or removed while the group is modifiable, and a user may not appear twice in the same group. Group tickets (F2-C04) build on these groups.

## What Changes

- Add the group concept (identifier, name, member users) managed by the park controller.
- Add `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email` and `GROUP REMOVE;Identifier;Email`.
- Reject duplicate group identifiers, unknown groups, unknown users, duplicate membership and removal of non-members.
- Determine group modifiability from its associated group tickets and the current system date (never stored as an attribute). No group tickets exist yet, so groups are currently always modifiable.
- Introduce the minimal registry of existing users (looked up by email) that group membership requires.
- Extend `HELP` with the new commands.
- Commit with the exact message `[F2-C02] Add user groups`.

## Capabilities

### New Capabilities
- `user-groups`: Creation of groups of existing users and management of their membership.

### Modified Capabilities
- `execution-control`: HELP additionally lists the GROUP CREATE/ADD/REMOVE syntax.

## Impact

- New `Group` and minimal `User` model classes, controller registries, GROUP commands, tests.
- **Flagged ambiguities / ordering conflicts:**
  - The batch applies this milestone before the user model (F1-C01), validation (F1-C02) and `USER CREATE` (F1-C03). Only the minimum needed for "users who already exist in the system" is added: a user identified by e-mail registered in the park controller. Until `USER CREATE` exists, users can only be registered programmatically (tests); `GROUP ADD` from commands reports unknown users.
  - `GROUP DELETE` and the ticket-based lock rules are described in the same source section but depend on group tickets; they are left to F2-C04 (group tickets), which the source also links to `GROUPTICKET DELETE`.
  - No format is specified for identifiers, names or e-mails in GROUP commands; only non-empty values are required. E-mail matching is exact.
  - Output texts are not specified; messages follow the existing `ERROR: ...` convention.
