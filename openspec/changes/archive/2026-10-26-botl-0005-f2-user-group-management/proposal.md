# Proposal

## Why

Delivery 2 introduces relationships between objects: park users must be organised into groups (families, schools…) that later buy group tickets. Groups must reference the registered users themselves and survive restarts.

## What Changes

- New commands `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`, listed in `HELP`.
- A group has an identifier, a name and a collection of existing users (references, not copies); a user cannot appear twice in a group and nonexistent users cannot be added.
- Groups and memberships are persisted in a text file under the Delivery 1 restrictions (standard library, `;`-separated text, no database) and restored at start-up.
- Deleting a user also removes that user from every group, so no group references a deleted user.
- A PlantUML class model (`uml/model.puml`) is added and kept up to date with the new classes, relationships and multiplicities.

## Capabilities

### New Capabilities
- `user-groups`: Creating, deleting and populating groups of registered users, and their persistence.
- `uml-model`: The project's PlantUML class model documenting the implemented design.

### Modified Capabilities
- `user-management`: deleting a user also removes it from all groups.

## Impact

- New classes `Group`, `GroupRepository`, `GroupManager`; `CommandProcessor` and `App` wiring updated.
- New runtime file `data/groups.txt`; new `uml/model.puml`.
- Tests for group rules, persistence/restore and user deletion.
