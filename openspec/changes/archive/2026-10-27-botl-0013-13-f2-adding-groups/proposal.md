# Proposal

## Why

Milestone F2-C02 lets registered users be organised into groups (later used for group tickets): a group has a unique identifier and a name, may start empty, and users are added or removed while the group is modifiable, never appearing twice in the same group.

## What Changes

- New commands `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, `GROUP DELETE;Identifier`.
- Group identifiers are unique; groups contain existing users only; a user cannot be added twice to the same group.
- The modifiability/deletion rule ("no associated group ticket dated on or after the current system date", derived from tickets rather than stored) is enforced through a single check that becomes effective when group tickets exist (F2-C04); until then every group is modifiable.
- HELP lists the four GROUP commands.
- Record the mandatory milestone commit `[F2-C02] Add user groups` once verified.

## Capabilities

### New Capabilities
- `group-management`: Groups of registered users — creation, membership changes, deletion and the conditions under which a group can be modified or deleted.

### Modified Capabilities
- `command-processing`: HELP additionally lists the GROUP commands.

## Impact

- New `Group`, `GroupRepository`, `GroupService`; `ParkController` gives access to groups; `CommandProcessor` dispatches GROUP commands; `UserService.deleteUser` removes the deleted user from groups.
- Group persistence is part of F2-C08 (groups live for the current execution until then).
