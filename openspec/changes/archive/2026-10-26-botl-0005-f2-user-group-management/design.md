# Design

## Context

Classes live in `es.upm.parque`: `User`, `UserManager` (rules + in-memory list), `UserRepository` (`data/users.txt`), `CommandProcessor` (dispatch), `CommandRunner`, `SystemDate`, `App` (wiring). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Groups hold references to the very `User` objects owned by `UserManager`.
- Same separation as users: model / manager (rules) / repository (file).

**Non-Goals:**
- Group tickets and locking (later change).
- A generic repository abstraction.

## Decisions

- **`Group`**: `id` (final), `name`, `List<User> members`; exposes `getMembers()` as an unmodifiable view plus `addMember`, `removeMember`, `hasMember`. Membership identity is object identity (users are unique objects in `UserManager`).
- **`GroupManager`**: holds groups in a `LinkedHashMap<String, Group>` (stable order), depends on `UserManager` to resolve emails to `User` objects; rejects invalid operations by throwing the existing `UserException`, which `CommandProcessor` already turns into `ERROR` lines. Alternative: a new `GroupException` — rejected as duplication.
- **`GroupRepository`**: `data/groups.txt`, one line per group: `id;name;dni1,dni2,...` (empty member field when no members). Loading takes a `Function<String, User>` resolving a DNI to the loaded user; unknown DNIs are skipped. DNI is used because it is immutable (emails can change).
- **Removing deleted users from groups**: `UserManager.delete` returns the removed `User`; `CommandProcessor` then calls `GroupManager.removeFromAllGroups(user)`. Keeps `UserManager` unaware of groups.
- **`uml/model.puml`**: class diagram of `User`, `UserField`, `Group` and the association `Group --> "0..*" User : members`. Validated with the PlantUML jar (`-checkonly`).

## Risks / Trade-offs

- [Group names cannot contain `;`] → inherent to the command format.
- [Users file and groups file written separately] → a crash between writes could leave a group pointing to a deleted user's DNI; loading skips unknown DNIs, so state self-heals.
