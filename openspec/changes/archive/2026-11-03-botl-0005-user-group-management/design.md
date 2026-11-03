# Design

## Context

Users live in `UserService` (in-memory list loaded through `UserFileStorage`). `CommandProcessor` is the main controller dispatching every command; `App` builds it per execution. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** groups that hold references to the registered `User` objects, strict membership rules, persistence that never duplicates user data.

**Non-Goals:** tickets, group-ticket locks, generic repositories or patterns for later deliveries.

## Decisions

- **`Group`** (state only): final `id`, `name`, `List<User> members` (insertion order); `contains(User)` compares by identity; `addMember`/`removeMember`; members exposed as an unmodifiable view.
- **`GroupService`**: holds `List<Group>` and a reference to `UserService` for email lookup. Operations `create`, `addMember`, `removeMember`, `delete`, `removeUserFromAll(User)`; violations raise `IllegalArgumentException`; each successful change saves through `GroupFileStorage`, reverting the in-memory change if saving fails (same approach as `UserService`). Group id comparison is exact (case-sensitive); id and name must be non-blank.
- **`GroupFileStorage`**: only class touching `groups.txt`. Line `Id;Nombre;DNI1,DNI2`. Members are referenced by **DNI** because it is immutable (email can change). `load(List<User> users)` resolves DNIs to the given user objects, so no copies are created; unknown DNIs are skipped with a warning; missing file → no groups.
- **User deletion cascade (assumption):** the source does not say what happens to memberships when a user is deleted. Leaving the reference would make the in-memory group point to a non-existent user and diverge from what is reloaded. `UserService.delete` now returns the removed `User`; `CommandProcessor` then calls `groups.removeUserFromAll(user)`. Recorded as an assumption; it adds no new command.
- **Data directory:** `App.run` receives a data directory (default `.`) and derives `users.txt` and `groups.txt` from it, so tests can isolate all files in a temp dir; the runtime location of `users.txt` is unchanged.
- **Commands** are added to the `CommandProcessor` switch with exact parameter counts (3, 3, 3, 2 tokens) and to the HELP list.
- **UML:** `uml/model.puml` documents current classes (App, CommandProcessor, User, UserField, UserValidator, UserService, UserFileStorage, Group, GroupService, GroupFileStorage) with associations and multiplicities; validated with the PlantUML CLI (`-checkonly`) as an external tool, not a project dependency.

## Risks / Trade-offs

- [Group id/name cannot contain `;`, members list uses `,`] → inherent in the formats; DNIs never contain commas.
- [Two files saved independently: a user deletion could save `users.txt` and then fail on `groups.txt`] → reported as an error; on next start unknown DNIs are ignored, so the state is still consistent.
