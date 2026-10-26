# Design

## Context

Users are managed by `UserManager` (in-memory list + `UserRepository` file). `CommandProcessor` dispatches commands; `App` wires components. Users are identified permanently by DNI (immutable), whereas emails can change.

## Goals / Non-Goals

**Goals:** group model with references to existing `User` objects; file persistence; consistent state when users are deleted.

**Non-Goals:** group tickets/locks (next changes), generic repository abstractions.

## Decisions

- **`Group`**: `id` (final), `name`, `List<User> members`; methods `addMember`, `removeMember`, `contains`, `getMembers` (unmodifiable view).
- **`GroupRepository`** stores one group per line: `Id;Nombre;DNI1,DNI2,...`. Members are stored by DNI because it never changes; on load the repository receives a `Function<String, User>` lookup (backed by `UserManager.findByDni`) to relink members to the loaded user objects. Unknown DNIs are skipped with a warning. Alternative: storing emails — rejected because emails can be updated.
- **`GroupManager`** (receives `GroupRepository` and `UserManager`) implements create/delete/add/remove and `removeUserFromAllGroups(User)`, saving after each mutation.
- **User deletion**: `UserManager.delete` now returns the deleted `User`; `CommandProcessor` calls `groupManager.removeUserFromAllGroups` afterwards. Alternative: making `UserManager` depend on `GroupManager` — rejected to avoid a circular dependency.
- **Identifiers** are case-sensitive and must not be empty; group names must not be empty.
- **PlantUML** `uml/model.puml` documents the current classes and relationships; it is plain text, no build tooling.

## Risks / Trade-offs

- [Group file edited by hand with unknown DNIs] → skipped with a warning instead of failing start-up.
