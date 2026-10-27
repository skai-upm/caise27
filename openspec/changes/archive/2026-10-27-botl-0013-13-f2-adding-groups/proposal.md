# Proposal

## Why

Milestone F2-C02 lets registered users be organised into groups (identifier, name, members), which later milestones use for group tickets and group attraction access.

## What Changes

- Add groups with a unique identifier, a name and a collection of member users (references to existing `User` objects).
- `GROUP CREATE;Identifier;Name` creates an empty group.
- `GROUP ADD;Identifier;Email` adds an existing user; a user already in the group is rejected.
- `GROUP REMOVE;Identifier;Email` removes a member.
- `GROUP DELETE;Identifier` deletes the group.
- The central park controller owns the groups.
- Commit with the exact message `[F2-C02] Add user groups`.

The restrictions based on active group tickets (no ADD/REMOVE/DELETE while a group ticket dated today or later exists, computed from tickets and the system date, never stored as a flag) depend on group tickets, which do not exist until F2-C04; they are specified and implemented there. With no group tickets, every group is modifiable and deletable.

## Capabilities

### New Capabilities
- `user-groups`: Groups of registered users and the GROUP CREATE/ADD/REMOVE/DELETE commands.

### Modified Capabilities
<!-- none -->

## Impact

- New `model/Group`, `repository/GroupRepository`; `ParkController` gains the groups; `CommandProcessor` handles GROUP commands; HELP lists them.
- Groups are kept in memory in this milestone; their persistence is F2-C08 ("Extend park persistence").
- Ambiguities flagged: group identifiers are compared exactly (case-sensitive); identifier and name must be non-empty; when a user is deleted with USER DELETE it is also removed from every group, so groups keep consisting only of existing users; response texts are `GROUP CREATED`, `GROUP USER ADDED`, `GROUP USER REMOVED`, `GROUP DELETED`.
