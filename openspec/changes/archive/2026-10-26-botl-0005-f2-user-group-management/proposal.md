# Proposal

## Why

Visitors often come as families or groups. Delivery 2 needs registered users to be organized into groups (later used for group tickets), introducing object relationships and collections that must survive restarts.

## What Changes

- New commands: `GROUP CREATE;<id>;<name>`, `GROUP ADD;<id>;<email>`, `GROUP REMOVE;<id>;<email>`, `GROUP DELETE;<id>`.
- A group has an identifier, a name and a collection of existing users (references to the same user objects, never copies).
- Only registered users can be added; a user cannot appear twice in the same group.
- Groups and memberships are persisted in a text file and restored at startup, linked to users by their immutable DNI.
- Deleting a user also removes them from every group so no group refers to a missing user.
- A PlantUML class model (`uml/model.puml`) is introduced and kept up to date with the domain classes, relationships and multiplicities.
- HELP lists the new commands.

## Capabilities

### New Capabilities
- `group-management`: creating/deleting groups, adding/removing members and group persistence.
- `domain-model`: the PlantUML class model of the project kept in the repository.

### Modified Capabilities
- `user-management`: deleting a user also removes them from all groups.

## Impact

- New classes `Group`, `GroupManager`, `GroupRepository`; `CommandProcessor`, `App` and the user deletion flow updated.
- New data file `data/groups.txt`.
- New file `uml/model.puml`.
