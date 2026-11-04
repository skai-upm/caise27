# Design

## Context

`UserManager` owns the in-memory `List<User>` loaded by `UserFileStore`; `CommandProcessor` (the main controller) dispatches commands; `App` wires stores for `users.txt`. `UserManager.update` currently saves a list containing a modified *copy* and then swaps it in, which would break shared references once other objects hold users. No PlantUML model exists yet. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Groups hold references to the live `User` objects; restart reconstructs the same sharing.
- Same persistence style and atomic "save first, then change memory" behavior as users.

**Non-Goals:**
- Group tickets, discounts, group size limits, group ownership/authentication (not in the source).

## Decisions

- **`Group`**: `id` (final), `name` (final), `List<User> members`; `contains(User)` by identity, `addMember`, `removeMember`, `getMembers()` unmodifiable view. Duplicate check by identity (the same registered user), which equals email/DNI uniqueness because users are unique.
- **`GroupManager`**: owns `List<Group>` and a reference to `UserManager` for lookups (`findByEmail`, `findByDni`). Operations return `Result`. For atomicity each operation builds the candidate list of groups (copying only the affected group via a `Group` copy constructor that shares the same `User` references), saves via `GroupFileStore`, and swaps state only on success.
- **`removeUserEverywhere(User)`** in `GroupManager`, invoked by `CommandProcessor` after a successful `USER DELETE` (the controller coordinates the two managers so `UserManager` stays unaware of groups). If no group contains the user, nothing is saved.
- **User identity preserved on update**: `UserManager.update` validates and builds a copy with the change, saves the list containing the copy, and on success applies the same change to the original instance (keeping the original in the list). Memory and file stay consistent; group references stay valid.
- **Persistence by DNI**: `GroupFileStore` writes `id;name;dni1;dni2...` (DNI is immutable while email can change). `load(Function<String, Optional<User>> byDni)` resolves members; unknown DNIs and lines with fewer than 2 fields are skipped with a warning on stderr.
- **Command parsing** reuses existing splitting; `GROUP CREATE` takes 2 params, `ADD`/`REMOVE` 2, `DELETE` 1; all nonblank.
- **Wiring**: `App.run(..., usersFile, groupsFile)`; production uses `users.txt` and `groups.txt` in the working directory. `CommandProcessor(UserManager, GroupManager, LocalDate)`; the two-argument convenience constructor uses `LocalDate.now()`.
- **UML**: `uml/model.puml` documents all current production classes and their associations; `Group "0..*" o-- "0..*" User : members` (aggregation: groups refer to users they do not own). Validated with the PlantUML jar (`-checkonly`), which is a verification tool only, not a project dependency.

## Risks / Trade-offs

- [User delete and group cleanup are two file writes] → users file is written first; if the groups write fails, the stale DNI is skipped with a warning at next load, so the invariant "members are registered users" still holds after restart.
- [Ambiguity: "update the PlantUML model" when none exists] → created `uml/model.puml` covering the current code; a later dedicated UML change can refine it.
- [Ambiguity: deleting a user that belongs to groups] → source is silent; removing the user from its groups is the smallest behavior that keeps groups consisting only of registered users.
