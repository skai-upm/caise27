# Proposal

## Why

Milestone F2-C02 introduces user groups, which later milestones use to buy group tickets. Registered users must be organisable into identified, named groups that can start empty and be modified progressively, with no user appearing twice in the same group.

## What Changes

- `GROUP CREATE;Identifier;Name` creates an empty group with a unique identifier.
- `GROUP ADD;Identifier;Email` adds an existing user to a group, rejecting users already in that group.
- `GROUP REMOVE;Identifier;Email` removes a member from a group.
- Groups are managed by the central park manager; deleting a user removes them from every group so that groups only contain existing users.
- Out of scope (later milestones, as stated by the assignment's roadmap): `GROUP DELETE` and the locking of groups by active group tickets (F2-C04, when group tickets exist), group persistence (F2-C08).

## Capabilities

### New Capabilities
- `user-groups`: Creation of groups and management of their membership.

### Modified Capabilities
- `user-deletion`: a successful deletion also removes the user from every group.

## Impact

- New `model.Group`, `repository.GroupRepository`, `service.GroupService`; `Park` exposes the groups; `UserService.deleteUser` updates groups; new commands in `CommandProcessor` and HELP.
