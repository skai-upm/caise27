# Proposal

## Why

Later park features (group tickets, discounts) operate on groups of visitors. Registered users must first be organisable into named groups whose memberships survive restarts and always refer to the real registered users, not copies.

## What Changes

- New `Group` with identifier, name and a collection of member users (references to the registered `User` objects).
- New commands: `GROUP CREATE;G01;Familia Garcia`, `GROUP ADD;G01;ana@correo.com`, `GROUP REMOVE;G01;ana@correo.com`, `GROUP DELETE;G01`.
- Rules: unique group identifier; only registered users can be added; a user cannot appear twice in the same group.
- Groups and memberships persisted in a `;`-separated text file and restored at startup, re-linking members to the loaded users.
- A deleted user is removed from every group (assumption, see design).
- `HELP` lists the GROUP commands.
- New PlantUML model `uml/model.puml` representing the classes, relationships and multiplicities.

## Capabilities

### New Capabilities
- `user-groups`: group creation/deletion, membership rules and the GROUP command contract.
- `group-file-persistence`: storage and restoration of groups and their member references.
- `domain-model-diagram`: the PlantUML class diagram kept in `uml/model.puml`.

### Modified Capabilities
- `application-control`: `HELP` also lists the four GROUP commands.

## Impact

- New classes `Group`, `GroupService`, `GroupFileStorage`; `CommandProcessor` gains GROUP commands; `App` wires the group service and stores data files in one data directory (default: working directory, so `users.txt` keeps its location); `UserService.delete` returns the removed user.
- New runtime file `groups.txt` (git-ignored). New file `uml/model.puml`.
- No change to user validation or `users.txt` format. No tickets or other future features.
