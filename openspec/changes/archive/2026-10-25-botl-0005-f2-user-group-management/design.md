# Design

## Context

`UserManager` keeps users in a `LinkedHashMap` keyed by DNI and persists them with `UserRepository` (`data/users.txt`). `CommandProcessor` dispatches with a `switch`, keeps `COMMAND_SYNTAX` for `HELP`, and owns the `SystemDate`. `App.start` wires everything with paths under `data/`.

## Goals / Non-Goals

**Goals:**
- Groups hold references to the same `User` objects managed by `UserManager`.
- Same structure as Delivery 1: model class, manager with rules, repository with the file format.

**Non-Goals:**
- Group tickets and group locking (next proposals), interfaces/patterns not needed yet.

## Decisions

- **`Group`**: `id` (final), `name`, `List<User> members` (`ArrayList`, insertion order); `addMember`, `removeMember`, `contains`, `getMembers()` returns an unmodifiable view.
- **`GroupManager`**: `LinkedHashMap<String, Group>`; operations return response strings like `UserManager`; persists through `GroupRepository` after each success; `removeUserFromAll(User)` used on user deletion.
- **Membership persisted by DNI**, not email: the DNI is immutable while the email can change, so the stored link survives `USER UPDATE;…;EMAIL`. `UserManager.findByDni` resolves DNIs at load time.
- **`GroupRepository.load(Function<String, User> byDni)`** receives the lookup instead of the `UserManager`, keeping the repository independent of business classes.
- **User deletion cascade** is coordinated by `CommandProcessor`: it looks up the user before `USER DELETE` and, if the response is `OK USER DELETED`, calls `groups.removeUserFromAll(user)`. Alternative: `UserManager` depending on `GroupManager` — rejected, users must not know groups.
- **Constructor of `CommandProcessor`** gains the `GroupManager`; tests build it with temporary files.
- **`uml/model.puml`** written by hand from the code: classes with their attributes (relationships drawn as associations, not repeated as attributes), `UserField` enum, the manager→model and manager→repository associations, and `Group "1" o--> "0..*" User : members`. Checked with the PlantUML jar (`-checkonly`).

## Risks / Trade-offs

- [`CommandProcessor` grows with coordination logic] → Acceptable for now; Delivery 3 reorganizes into layers and controllers.
- [Group names cannot contain `;` and member lists use `,`] → DNIs never contain commas; names are limited by the command format itself.
