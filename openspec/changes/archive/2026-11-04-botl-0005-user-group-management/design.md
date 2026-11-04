# Design

## Context

Users are managed by `UserManager` (list of `User`, saved through `UserFileStorage` to `users.txt`). `UserManager.update` currently replaces the `User` in its list with an edited copy; with groups holding references, that would leave stale objects. Commands are dispatched by `CommandProcessor`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Groups hold references to the same `User` instances managed by `UserManager`.
- Same persistence style as Delivery 1 (plain text, `;`, separate storage class).

**Non-Goals:**
- Group tickets, locks, group-level business rules beyond membership.

## Decisions

- **Classes:** `Group` (final `id`, `name`, `List<User> members`; `contains`, package-level `add/remove`), `GroupFileStorage` (`load(Function<String, User> userByDni)`, `save(Collection<Group>)`), `GroupManager` (ordered `LinkedHashMap<String, Group>`, operations, saves after success, uses `UserManager.findByEmail` to resolve members).
- **Reference by DNI in the file:** DNI is immutable while email may change, so `id;name;dni1;dni2...`. On load each DNI is resolved to the already loaded `User` (`UserManager.findByDni`), guaranteeing no copies; unknown DNIs are ignored.
- **Atomic group changes:** each operation builds the new state on a copy (new map / new `Group` with copied member list), saves it, then swaps it in. A failed save leaves memory unchanged.
- **In-place user update:** `UserManager.update` still validates and saves a list containing an edited copy; only after the save succeeds is the copy's editable state applied to the original object (`User.copyEditableFrom`) so every holder of the reference sees the update.
- **User deletion:** `UserManager.delete` returns the removed `User`; `CommandProcessor` then calls `GroupManager.removeUserFromAllGroups(user)`, which saves groups only if a membership changed. Alternative of `UserManager` knowing about groups rejected to keep the user component independent.
- **Wiring:** `App` creates `UserManager` first, then `GroupManager(new GroupFileStorage("groups.txt"), userManager)`, and passes both to `CommandProcessor`. Tests use an overload of `App.run` receiving both file paths.
- **PlantUML:** `docs/uml/model.puml` hand-written, listing domain, manager, storage and command classes with associations and multiplicities (`Group "0..*" o-- "0..*" User : members`).

## Risks / Trade-offs

- [Two files can disagree after a crash between saves of users and groups] → On load unknown DNIs are ignored, so the state self-heals.
- [Group identifiers are case-sensitive and unconstrained in format] → Source gives no format; the smallest rule (nonblank, unique) is used. Open question for later deliveries.
