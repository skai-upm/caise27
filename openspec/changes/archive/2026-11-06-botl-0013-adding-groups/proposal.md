# Proposal

## Why

Milestone F2-C02 (Delivery 2, step 2) requires organizing registered users into groups: groups are created with an identifier and a name (possibly empty), users are added or removed while the group is modifiable, the same user never appears twice in a group, and groups can be deleted under the documented ticket conditions.

## What Changes

- New commands `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`, `GROUP DELETE;Identifier`.
- Group identifiers are unique; members must be existing users; a user cannot be added twice to the same group.
- A group is locked (no `GROUP ADD`/`GROUP REMOVE`) and cannot be deleted while it has an *active* group ticket (date ≥ current system date). This state is derived from tickets and the current date, never stored. Group tickets themselves arrive in F2-C04; until then no group has an active ticket.
- Groups are reachable through the central park management; `HELP` lists the new commands.

### In scope
- Source section 5.

### Out of scope
- Group tickets and `GROUPTICKET DELETE` (F2-C04), group persistence (F2-C08).

## Capabilities

### New Capabilities
- `user-groups`: creating, populating, depleting and deleting groups of existing users, and the derived locked state.

### Modified Capabilities
- `execution-control`: `HELP` also lists the `GROUP` commands.

## Impact

- New `Group` model, `GroupRepository`, `GroupService`, `ParkException` base for domain errors; `ParkController` exposes the group service; `CommandProcessor` dispatches `GROUP` commands.
