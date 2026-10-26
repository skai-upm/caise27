# Proposal

## Why

Visitors often come together (families, schools). Later ticket features need to treat several registered users as one unit, so the application must let existing users be organised into persistent groups — the first relationship between domain objects.

## What Changes

- New commands `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`.
- A group has an identifier, a name and a collection of members that are the existing registered user objects (no copies).
- Only registered users can be added; a user cannot appear twice in the same group; group identifiers are unique.
- Groups and memberships are stored in their own text file (same Delivery 1 rules: standard library only, `;` separated, no database) and restored at startup, linking members to the loaded users.
- Deleting a user also removes them from every group, so no group refers to a non-existent user.
- HELP lists the group commands.
- A PlantUML class model (`docs/uml/model.puml`) is added describing the current classes, relationships and multiplicities, including the new group classes.

## Capabilities

### New Capabilities
- `group-management`: Creating/deleting groups, adding/removing registered users, membership rules and group persistence.
- `domain-model`: The PlantUML class model kept in the repository describing the application's classes, relationships and multiplicities.

### Modified Capabilities
- `command-processing`: HELP lists the GROUP commands.
- `user-management`: deleting a user also removes them from all groups.

## Impact

- New classes `Group`, `GroupManager`, `GroupStorage`; `CommandProcessor` routes `GROUP` commands; `UserManager` exposes lookup by email and returns the deleted user.
- New data file `data/groups.txt` (already git-ignored via `data/`).
- New `docs/uml/model.puml`.
