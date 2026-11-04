# Proposal

## Why

Users can only be managed individually. The park needs groups of already registered users (for example a family), with membership changes and durable restoration, introducing object relationships and collections that later group features build on.

## What Changes

- New group concept: identifier, name and a collection of members that are the registered user objects themselves (no copies).
- New commands: `GROUP CREATE;G01;Familia Garcia`, `GROUP ADD;G01;ana@correo.com`, `GROUP REMOVE;G01;ana@correo.com`, `GROUP DELETE;G01`.
- Nonexistent users cannot be added; a user cannot appear twice in the same group; group identifiers are unique.
- Groups and memberships persist in a semicolon-separated text file (Java standard library only) and are restored on restart, linked to the users loaded from the users file.
- Updating a user keeps the same user object so its groups see the change; deleting a user removes it from every group.
- `HELP` lists the four group commands.
- A PlantUML class model `uml/model.puml` represents the current classes, including the new group classes, relationships and multiplicities.

The source requests the change name `manage-user-groups`; this batch uses the BOTL ID `botl-0005-5-f2-user-group-management`.

## Capabilities

### New Capabilities
- `user-groups`: Group model, the four GROUP commands and membership rules.
- `group-file-persistence`: Text-file storage and restart restoration of groups and memberships.
- `uml-model`: The PlantUML class model of the application kept in the repository.

### Modified Capabilities
- `application-control`: `HELP` also lists the group commands.

## Impact

- New classes `Group`, `GroupManager`, `GroupFileStore`; `CommandProcessor` dispatches `GROUP` commands; `App` wires a `groups.txt` store.
- `UserManager`: user lookup by email/DNI; `update` mutates the stored user instance (after a successful save) instead of replacing it; delete triggers removal from groups via the controller.
- New file `uml/model.puml`; new runtime data file `groups.txt` (not committed).
