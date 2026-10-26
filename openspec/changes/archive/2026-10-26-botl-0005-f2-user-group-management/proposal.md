# Proposal

## Why

Visitors often come as families or teams, and Delivery 2 will sell tickets to groups. The system first needs to organise existing users into persistent groups, introducing object relationships and collections between domain objects.

## What Changes

- New commands:
  - `GROUP CREATE;<id>;<name>` — creates an empty group with a unique identifier.
  - `GROUP ADD;<id>;<email>` — adds an existing registered user to the group.
  - `GROUP REMOVE;<id>;<email>` — removes a member from the group.
  - `GROUP DELETE;<id>` — deletes the group (its users remain registered).
- A group refers to the registered user objects themselves, never to copies; a user cannot be added twice to the same group, and unregistered emails cannot be added.
- Groups and their memberships are persisted in a text file under the Delivery 1 constraints (standard library, no database, `;`-separated lines) and restored at startup.
- When a user is deleted, they are removed from every group so no group refers to a non-existent user.
- HELP lists the new commands.
- A PlantUML class model `uml/model.puml` is created with the current classes, including the new group classes, their relationships and multiplicities.

## Capabilities

### New Capabilities
- `group-management`: Creating and deleting groups of registered users, adding and removing members, membership rules, and persistence of groups between executions.
- `design-model`: The PlantUML class model of the application kept in `uml/model.puml`, which must reflect the implemented classes, relationships and multiplicities.

### Modified Capabilities
<!-- None: HELP already lists all supported commands; user rules are unchanged. -->

## Impact

- New classes `Group`, `GroupRepository`, `GroupManager`; `CommandProcessor` gains the GROUP commands and removes deleted users from groups; `App` loads groups after users.
- New data file `data/groups.txt`; new `uml/model.puml`.
- Tests for group rules, persistence and commands. No new dependencies.
