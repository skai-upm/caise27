# Design

## Context

Current classes (`com.parque`): `User`, `UserField`, `UserValidator`, `UserRepository` (`users.txt`), `UserService` (in-memory list + save-after-change), `CommandProcessor` (controller: dispatch, HELP, EXIT, system date), `App` (input modes; wires everything with a `users.txt` path). `UserService.update` currently replaces the `User` object with an edited copy, which would break references held by groups.

## Goals / Non-Goals

**Goals:** groups referencing the same `User` instances; restore relationships at startup; keep both files consistent after every command.

**Non-Goals:** tickets, group-ticket locks, discounts; renaming groups; listing commands.

## Decisions

- **Model:** `Group` with `final String id`, `String name`, `List<User> members` (insertion order), encapsulated: `getMembers()` returns an unmodifiable view; `addMember`/`removeMember`/`contains` operate by reference identity.
- **Service:** `GroupService(GroupRepository, UserService)` loads groups via the repository using `UserService.findByDni` as the resolver. Methods `create`, `addMember(id, email)`, `removeMember(id, email)`, `delete(id)`, `removeUserFromAllGroups(User)`; errors as `IllegalArgumentException` like `UserService`.
- **Atomicity:** each operation builds the would-be state as a list of lines via `GroupRepository.save(List<Group>)` called on a *copy* (shallow copies of affected groups sharing the same `User` references), and swaps it in only after the file was written — same pattern as `UserService.commit`.
- **Persistence by DNI:** `Identificador;Nombre;DNI,DNI`. DNI is immutable, so email changes never invalidate the file. On load, unknown DNIs and malformed lines are skipped with a message on standard error (consistent with `UserRepository`).
- **In-place user update:** `UserService.update` saves a list containing an edited copy first (so a failed write changes nothing), then applies the change to the original `User` object instead of replacing it. Same validations as before.
- **Cascade on user deletion:** `CommandProcessor` handles `USER DELETE` by calling `users.delete(...)`, which now returns the deleted `User`, then `groups.removeUserFromAllGroups(user)`. Kept in the controller so `UserService` does not depend on groups (dependency direction groups → users).
- **Data directory:** `App.run` receives a data directory instead of the users file path; it uses `<dir>/users.txt` and `<dir>/groups.txt`. Production uses the working directory, so existing `users.txt` locations remain compatible.
- **UML:** hand-written `uml/model.puml` (PlantUML class diagram) with all current classes, `Group "0..*" o-- "0..*" User : members`, and dependencies between controller, services and repositories.

## Risks / Trade-offs

- [Two files are written by one `USER DELETE`] → If the group file write fails after the user file write, memberships of the deleted user remain in `groups.txt`; they are ignored on next load (unknown DNI), so state stays consistent for the user.
- [Hand-maintained UML can drift] → Tasks of each change that alters classes update it; a test checks that every production class is declared in the model.
