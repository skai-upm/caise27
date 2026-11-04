# Design

## Context

Users are managed by `UserManager` (in-memory list mirrored to `users.txt` via `UserFileRepository`). `CommandProcessor` is the controller and dispatches commands, and `App.run` wires everything with an injectable users-file path. A user's email can change, but the DNI is immutable.

## Goals / Non-Goals

**Goals:**
- Groups hold references to the `User` objects owned by `UserManager`.
- Same responsibility split as users: entity / manager (rules) / file repository.
- Persisted memberships survive email changes.

**Non-Goals:**
- Group tickets, discounts, ticket-related locks.
- Authenticating group commands (the source specifies no credentials).

## Decisions

- **`Group`**: `id` (final), `name`, `List<User> members` (insertion order). `addMember` / `removeMember` / `contains` compare members by **identity** (`==`). That works because all users come from `UserManager`, and identity also matches by DNI, since there is one object per DNI. `getMembers()` returns an unmodifiable view.
- **`GroupManager`**: `LinkedHashMap<String, Group>` keyed by id (exact match, case-sensitive), with `create`, `addMember(id, email)`, `removeMember(id, email)`, `delete(id)`, `removeUserEverywhere(User)`. It looks users up through `UserManager.findByEmail`, so no copies are created. After each successful change it saves the groups through the repository. If the save fails, it restores the previous state (snapshot of the affected group or map entry) and throws `UserException`, mirroring `UserManager`. Rejections reuse `UserException` as the general "command rejected" exception, so no new exception type is needed.
- **`GroupFileRepository`**: line format `Id;Nombre;DNI1;DNI2;...`, written with `String.join`. `load(Function<String, User> userByDni)` resolves DNIs through a lookup supplied by `GroupManager` (built from `UserManager.getUsers()`), so the repository never creates `User` objects. Unknown DNIs are skipped with a warning on stderr. Lines with fewer than two fields are skipped. Memberships are stored by DNI rather than email because the DNI is immutable while the email is editable.
- **User deletion consistency (derived rule).** The source forbids non-existent users in groups but does not say what happens on `USER DELETE`. Leaving dangling members would break that invariant and the restart behaviour, so `CommandProcessor` calls `groups.removeUserEverywhere(deletedUser)` after a successful `USER DELETE`. `UserManager.delete` now returns the deleted `User`. If the groups file then cannot be saved, the command reports `ERROR` while the user deletion stands. That case is extremely unlikely and is documented here.
- **Wiring.** `App.run` derives `groups.txt` as a sibling of the users file (`usersFile.resolveSibling("groups.txt")`), so tests keep isolated temp files. `CommandProcessor` constructors become `(UserManager, GroupManager)` and `(UserManager, GroupManager, LocalDate)`.
- **PlantUML model** at `docs/uml/model.puml`, written by hand to reflect the code: classes with key attributes and operations, `UserManager "1" *-- "0..*" User`, `GroupManager "1" *-- "0..*" Group`, `Group "0..*" o-- "0..*" User : members`, and dependencies of the repositories and controller.

## Risks / Trade-offs

- [Two files can drift if one save fails] → each manager rolls back its own state. The cross-file case (user deleted, groups save failed) is reported as an error.
- [Case-sensitive group ids] → smallest behaviour consistent with the source. `G01` and `g01` are different groups.

## Open Questions

- Whether empty groups should be deleted automatically when the last member leaves: not requested, so groups persist until `GROUP DELETE`.
