# Design

## Context

`ParkController` (singleton) holds the users, date and price. Delivery 2 requires relationships as objects and collections in memory: a group holds its `User` objects; users do not know their groups or tickets.

## Goals / Non-Goals

**Goals:**
- Group model, repository and service; four GROUP commands; one place deciding whether a group is modifiable.

**Non-Goals:**
- Group tickets (F2-C04) and group persistence (F2-C08).

## Decisions

- **`Group`**: `final String identifier`, `String name`, `List<User> members` (insertion order), methods `contains(User)`, `addMember`, `removeMember`, `getMembers()` (unmodifiable view). Identity comparison of `User` objects (one object per user in memory).
- **`GroupRepository`**: in-memory `LinkedHashMap<String, Group>` (save, findById, delete, findAll, `removeUserFromAll(User)`), registered in `ParkController.initialize(users, groups, today)`; the two-argument `initialize` keeps an empty group repository.
- **`GroupService`** (uses `ParkController.getInstance()`): create/add/remove/delete with `UserOperationException`-style failures (`GroupOperationException`). All modifications call `checkModifiable(group)`, which asks `ParkController.hasActiveGroupTicket(group)`: with no group tickets yet it returns `false`; F2-C04 implements it from group tickets and the system date. No stored "locked" flag.
- **User deletion:** `UserService.deleteUser` also calls `groups.removeUserFromAll(user)` through the controller, keeping "groups consist of existing users" true (ambiguity, flagged: the source does not state this explicitly for deletion).
- **Validation (flagged):** identifiers and names must be non-empty; no other format rules are invented.
- **Output:** `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`, `GROUP DELETED`; errors `ERROR: ...`.
- **Mandatory commit:** `[F2-C02] Add user groups` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Lock rule not yet observable] → Enforcement point exists and is covered when tickets arrive in F2-C04.
