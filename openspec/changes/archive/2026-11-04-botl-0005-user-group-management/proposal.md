# Proposal

## Why

Visitors come to the park in families or groups. The application can manage individual users only; it needs groups made of existing users, kept across runs, as the basis for later group features. Doing so also introduces object relationships and collections into the model.

## What Changes

- New `Group` entity with an identifier, a name and a collection of members that are the **same** user objects managed by the system (no copies).
- New commands: `GROUP CREATE;G01;Familia Garcia`, `GROUP ADD;G01;ana@correo.com`, `GROUP REMOVE;G01;ana@correo.com`, `GROUP DELETE;G01`.
- Rules: unique group identifiers; only registered users can be added; a user appears at most once per group.
- Groups and memberships are stored in a semicolon-separated text file and restored at startup by linking to the loaded users.
- When a user is deleted, they are removed from every group, so no group refers to a non-existent user (derived rule, see design).
- `HELP` lists the group commands.
- A PlantUML class model (`docs/uml/model.puml`) is created, since none exists yet, and represents the current classes, including the new ones, their relationships and multiplicities.

## Capabilities

### New Capabilities
- `user-groups`: group entity, the four `GROUP` commands, membership rules, consistency with user deletion.
- `group-file-persistence`: text-file format of groups and memberships and restart recovery.
- `domain-model-diagram`: the maintained PlantUML class model of the application.

### Modified Capabilities
- `application-control`: `HELP` must list the four `GROUP` commands.

## Impact

- New classes `Group`, `GroupManager`, `GroupFileRepository`; `CommandProcessor` dispatches `GROUP` commands and removes memberships on `USER DELETE`; `App` wires the groups file (`groups.txt`, next to `users.txt`).
- `users.txt` format and all user commands unchanged.
- New file `docs/uml/model.puml`. `groups.txt` is added to `.gitignore`. No new dependencies.
- Out of scope: tickets, group tickets, discounts.
