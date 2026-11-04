# Proposal

## Why

Users currently exist only individually. Later features (group tickets and discounts) need users organised into groups, so this change introduces groups that reference already-registered users, the commands to manage them, and file persistence that restores the relationships on restart.

## What Changes

- New `Group` with an identifier, a name and a collection of members that are the existing `User` objects (no copies).
- New commands `GROUP CREATE;G01;Familia Garcia`, `GROUP ADD;G01;ana@correo.com`, `GROUP REMOVE;G01;ana@correo.com`, `GROUP DELETE;G01`, listed in `HELP`.
- Rules: unique group identifier; only registered users can be added; a user cannot appear twice in the same group.
- New `groups.txt` file next to `users.txt`; each group is one line and members are stored by DNI (immutable), then resolved to the loaded users at startup.
- `USER UPDATE` now modifies the stored `User` object in place, so groups keep referencing it after the email or any other field changes.
- `USER DELETE` also removes the deleted user from every group's member list (so no group references a user that no longer exists).
- New PlantUML class model `uml/model.puml` representing the classes, relationships and multiplicities, including the new ones.

## Capabilities

### New Capabilities
- `user-groups`: Group data, the four `GROUP` commands and their membership rules.
- `group-file-persistence`: Group file format, saving after each change and restoring group–user relationships at startup.
- `uml-model`: The PlantUML class model kept in the repository and its required content.

### Modified Capabilities
- `user-management`: "Delete user" additionally removes the deleted user from all groups.

## Impact

- **Code**: new `Group`, `GroupRepository`, `GroupService`; `CommandProcessor` (dispatch, HELP), `UserService.update` (in-place change), `App` (wires the group components; the data directory now holds both files).
- **Data**: new `groups.txt` in the working directory; `users.txt` unchanged and compatible.
- **Docs**: new `uml/model.puml`.
- **Open questions / assumptions**: the source does not say what happens to memberships when a user is deleted — we remove them (smallest behaviour that avoids dangling references); group identifiers are case-sensitive, non-blank and unique; group names are non-blank but need not be unique; `GROUP DELETE` deletes a group even if it has members (users are untouched); the source does not specify response texts (`OK: Group created`, etc.); no PlantUML model existed before, so one is created.
