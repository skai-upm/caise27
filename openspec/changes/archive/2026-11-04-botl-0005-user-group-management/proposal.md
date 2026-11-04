# Proposal

## Why

Users currently exist only individually. The park needs groups (e.g. a family) made of already registered users, introducing object relationships and collections that later group tickets build on, and these relationships must survive restarts.

## What Changes

- New `Group` entity with identifier, name and a collection of references to existing `User` objects (no copies).
- New commands: `GROUP CREATE;Id;Nombre`, `GROUP ADD;Id;Correo`, `GROUP REMOVE;Id;Correo`, `GROUP DELETE;Id`.
- Rules: unique group identifiers; only registered users can be added; a user appears at most once per group; removing a non-member or acting on an unknown group is rejected.
- Groups and memberships persist in a semicolon-separated `groups.txt` file (members referenced by immutable DNI) and are restored at startup against the loaded users.
- Deleting a user removes it from every group, so no group references a nonexistent user.
- User updates now modify the existing `User` object in place (after a successful save) so groups keep seeing current data.
- `HELP` lists the four group commands.
- A PlantUML class model (`docs/uml/model.puml`) is added representing the current classes, relationships and multiplicities, including the new group relation.

## Capabilities

### New Capabilities
- `user-groups`: Group entity, the four `GROUP` commands, membership rules and the effect of user deletion on memberships.
- `group-file-persistence`: Text-file storage of groups and their members and restoration at startup.
- `uml-model`: PlantUML class model of the application kept consistent with the code.

### Modified Capabilities
- `application-control`: "HELP command" lists the group commands.

## Impact

- New: `Group`, `GroupManager`, `GroupFileStorage`, tests, `docs/uml/model.puml`.
- Changed: `CommandProcessor` (group commands, constructor takes `GroupManager`), `UserManager` (in-place update; delete returns the removed user), `User` (copy of editable fields), `App` (wires `groups.txt`).
- Existing user commands, validation and `users.txt` format unchanged.
