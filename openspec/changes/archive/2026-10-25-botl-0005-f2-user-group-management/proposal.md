# Proposal

## Why

Visitors often come in families or parties. Delivery 2 needs existing users to be organized into groups (later used for group tickets), introducing object relationships and collections while keeping all Delivery 1 behavior intact.

## What Changes

- Groups with an identifier, a name and a collection of members that are the existing `User` objects (no copies).
- Commands `GROUP CREATE;<id>;<name>`, `GROUP ADD;<id>;<email>`, `GROUP REMOVE;<id>;<email>`, `GROUP DELETE;<id>`.
- Only registered users can be added; a user cannot appear twice in the same group.
- Groups and their memberships persist in a text file (same restrictions as Delivery 1: plain files, no external libraries) and are restored on startup, linked to the restored users.
- When a user is deleted, the user is removed from every group so no group references a non-existent user.
- `HELP` lists the four new commands.
- A PlantUML class model `uml/model.puml` is introduced and represents the current classes, relationships and multiplicities, including the new group classes.

## Capabilities

### New Capabilities
- `user-groups`: Group data, the GROUP commands and their rules, group persistence and consistency with user deletion.
- `design-model`: The PlantUML class model kept in the repository and its consistency with the code.

### Modified Capabilities
- `command-processing`: the `HELP` requirement lists the GROUP commands.

## Impact

- New classes `Group`, `GroupManager`, `GroupRepository`; `UserManager` gains lookup by DNI; `CommandProcessor` gains the GROUP commands and coordinates group cleanup on user deletion; `App` wires the new components and the `data/groups.txt` file.
- New file `uml/model.puml`.
- New tests; existing Delivery 1 tests keep passing.
