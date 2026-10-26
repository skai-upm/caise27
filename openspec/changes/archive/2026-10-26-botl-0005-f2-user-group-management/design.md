# Design

## Context

Users are managed by `UserManager` (in-memory list persisted through `UserRepository` to `data/users.txt`). `CommandProcessor` dispatches commands and `App.main` wires everything together with a `MainController` holding the system date. `ParkException` carries user-facing rejection messages.

## Goals / Non-Goals

**Goals:**
- Groups hold references to the same `User` instances managed by `UserManager`.
- Group persistence that survives email changes.

**Non-Goals:**
- Group tickets and group locking (next changes).

## Decisions

- **`Group`**: `id` (final), `name` (final), `List<User> members`; `addMember`, `removeMember`, `hasMember` (identity comparison), `getMembers()` returns an unmodifiable view.
- **`GroupRepository`** (`data/groups.txt`): line format `id;name;dni1,dni2,...` (members last, possibly empty). `loadAll(Map<String, User> usersByDni)` resolves DNIs to the loaded users and silently drops DNIs that no longer exist. Alternative: identify members by email — rejected because emails can change through `USER UPDATE`.
- **`GroupManager`**: receives `GroupRepository` and `UserManager` (to look up users by email and build the DNI map at load time). Operations `create`, `delete`, `addMember`, `removeMember`, `removeUserFromAllGroups(User)`, each persisting on success and reloading from file on save failure, mirroring `UserManager`.
- **User deletion cascade**: `UserManager.delete` returns the deleted `User`; `CommandProcessor` then calls `groupManager.removeUserFromAllGroups(user)`. Alternative: `UserManager` depending on `GroupManager` — rejected to avoid a cyclic dependency (groups already depend on users).
- **`CommandProcessor`** gets the `GroupManager` and the four new syntax constants (listed by HELP). To keep `process` readable the switch delegates each command to a small private method.
- **UML**: `uml/model.puml` lists domain classes (`User`, `UserField`, `Group`) with attributes and the application classes (`App`, `CommandProcessor`, `MainController`, managers, repositories) with their associations, named after the holding attribute and with multiplicities. Validated with the PlantUML jar (`-checkonly`).

## Risks / Trade-offs

- [Group file references a DNI whose user was removed outside the application] → Dropped at load; the next save rewrites a consistent file.
