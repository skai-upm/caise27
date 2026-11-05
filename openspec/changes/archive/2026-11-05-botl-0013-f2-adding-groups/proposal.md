# Proposal

## Why

Milestone F2-C02 (Delivery 2, step 2) lets registered users be organized into groups: a group has a unique identifier and a name, can start empty, and users can be added or removed while it is modifiable. The same user can never appear twice in a group (source §5). Group tickets, which lock a group, come in a later milestone, but the lock rule must already be part of the group operations so that they need no rework later. Today the system has no groups.

## What Changes

- Add `GROUP CREATE;Identifier;Name` to create an empty group with a unique, non-empty identifier and a name.
- Add `GROUP ADD;Identifier;Email` to add an existing user to an existing group. A user already in the group is rejected.
- Add `GROUP REMOVE;Identifier;Email` to remove a member from a group.
- Add `GROUP DELETE;Identifier` to delete a group.
- `GROUP ADD`/`GROUP REMOVE` are allowed only while the group has no active group ticket, and `GROUP DELETE` only when no associated group ticket is dated on or after the current system date. The locked state is derived from tickets and the system date, never stored. No group tickets exist until the "group tickets" milestone, so for now every group is modifiable and deletable.
- Groups are reached through the central park management object and kept in memory. Persistence comes in the "complete park persistence" milestone.
- Deleting a user (`USER DELETE`) also removes that user from every group, so groups only ever contain existing users.
- List the group commands in `HELP`.

In scope: the four group commands, membership rules, the derived lock check, and membership cleanup on user deletion.

Out of scope: group tickets and `GROUPTICKET DELETE` (later milestone), and group persistence.

## Capabilities

### New Capabilities
- `user-groups`: Groups of registered users — creation, membership with no duplicates, removal, deletion, and the derived "locked by active group ticket" rule.

### Modified Capabilities
- `user-deletion`: adds the requirement that a deleted user is removed from every group they belonged to.

## Impact

- New `model.Group`, `repository.GroupRepository`, `command.GroupCommands`; `ParkController` holds the groups and offers the lock check; `CommandProcessor` dispatches the group commands; tests.
