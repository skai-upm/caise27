# Proposal

## Why

Milestone F2-C02 lets registered users be organized into groups. Groups are the basis for group tickets and group access in later Delivery 2 milestones. The central park controller exists (`park-management`) but holds no groups, and there are no GROUP commands.

## What Changes

- Add user groups, each with a unique identifier and a name, stored by the central park controller. A group can be created empty.
- Add `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email` and `GROUP DELETE;Identifier`.
- GROUP ADD accepts only users who exist in the system and rejects a user who is already a member of that group.
- A group is *locked* when it has an active group ticket, meaning an associated group ticket dated on or after the current system date. GROUP ADD and GROUP REMOVE are rejected while the group is locked. GROUP DELETE is rejected when the group has any associated group ticket dated on or after the current date. The locked state is derived from the tickets and the current date and is never stored as an attribute.
- List the GROUP commands in HELP.

### In scope
- Group model and its rules, the four GROUP commands, and derivation of the lock from group tickets and the system date.

### Out of scope
- User registration and the user model (Delivery 1 user milestones). Groups use the central controller's user-existence lookup. Until those milestones register users, no user exists, so GROUP ADD is rejected for every email.
- Group tickets and `GROUPTICKET DELETE` (group ticket milestone). Until group tickets exist, no group has associated tickets, so every group is modifiable and deletable. The lock rule is implemented now and takes effect as soon as group tickets are recorded.

## Capabilities

### New Capabilities
- `user-groups`: creating, populating, shrinking and deleting groups of registered users, including the ticket-based lock rules.

### Modified Capabilities
- None. The central controller's single-instance requirement (`park-management`) already covers it owning groups.

## Impact

- New `es.upm.park.model.Group`. `ParkController` gains group storage, a user-existence lookup and a group-ticket-date lookup.
- `CommandProcessor`: GROUP handlers and HELP lines.
- New unit tests. No persistence (complete park persistence is a later milestone).
