# Design

## Context

See proposal.md – Why. `ParkController` (single instance) owns the per-execution services; domain errors are `UserException`, caught by `CommandProcessor`. Group tickets do not exist yet (F2-C04).

## Goals / Non-Goals

**Goals:**
- Locked state computed on demand from tickets and the current date.
- Group membership robust to email changes.

**Non-Goals:**
- Persisting groups (F2-C08); cascading user deletion into groups (not required by the source).

## Decisions

- **`Group` model**: final `id`, `name`, ordered list of member DNIs (DNI is immutable, emails may change through `USER UPDATE`). Methods `contains/addMember/removeMember/getMemberDnis` (unmodifiable).
- **`GroupService(GroupRepository, UserRepository, Predicate<Group> activeTicket)`**: the predicate answers "has an associated ticket dated ≥ current date". `ParkController.start` supplies it; until group tickets exist it is `g -> false`, and F2-C04 replaces it with a query over group tickets and `getCurrentDate()`. Alternative: a `locked` flag on `Group` — forbidden by the source.
- **`ParkException`** (new base `RuntimeException` in `es.upm.park.service`); `UserException` and the new `GroupException` extend it; `CommandProcessor` catches `ParkException`.
- **Messages**: `Group created: G01`, `User <email> added to group G01`, `User <email> removed from group G01`, `Group deleted: G01`; errors `group G01 already exists`, `no group G01`, `no user with email X`, `user X already in group G01`, `user X not in group G01`, `group G01 has an active group ticket`.

## Risks / Trade-offs

- [Locking cannot be exercised end-to-end until F2-C04] → unit tests drive the predicate directly.
