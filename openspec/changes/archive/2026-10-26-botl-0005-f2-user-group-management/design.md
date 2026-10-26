# Design

## Context

Users live in `UserManager` (map keyed by DNI) persisted by `UserStorage`; `CommandProcessor` dispatches two-word commands by domain. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Groups reference the very `User` objects held by `UserManager`; persistence mirrors the Delivery 1 approach.

**Non-Goals:**
- Group listing/query commands, group tickets (later delivery), authentication for group commands (not requested).

## Decisions

- **`Group`**: `id` (final), `name`, `List<User> members` (insertion order; duplicate check by identity). Exposes an unmodifiable view.
- **`GroupManager`**: `LinkedHashMap<String, Group>`; needs `UserManager` to resolve emails (`findByEmail`) and DNIs; persists through `GroupStorage` after every change. `removeUserFromAll(User)` for user deletion.
- **`GroupStorage`** file `data/groups.txt`, one group per line: `Id;Nombre;DNI1,DNI2,...`. Members stored by **DNI** because it is immutable (emails can change). On load, DNIs are resolved through the user map; unknown DNIs are skipped. Alternative: separate membership file — rejected as unnecessary for this size.
- **User deletion cascade** handled in `CommandProcessor`: `UserManager.delete` now returns the removed `User`, then `GroupManager.removeUserFromAll(user)` runs. Keeps `UserManager` unaware of groups.
- **UML**: hand-written `docs/uml/model.puml` covering all classes (App, CommandProcessor, UserManager, UserStorage, User, UserField, UserValidator, Group, GroupManager, GroupStorage) with associations and multiplicities.

## Risks / Trade-offs

- [Group file edited by hand with unknown DNI] → member skipped on load; group kept.
- [User and group files written separately] → a crash between writes could leave a stale DNI in groups.txt; tolerated by the skip-on-load rule.
