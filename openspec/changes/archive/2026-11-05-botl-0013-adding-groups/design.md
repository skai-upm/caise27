# Design

## Context

`Park` (singleton) holds the date, price and users. Commands are registered in `CommandProcessor` with element counts and usage strings.

## Goals / Non-Goals

**Goals:** groups owned by `Park`, membership rules in a service. **Non-Goals:** ticket-based locking and GROUP DELETE (F2-C04), persistence (F2-C08).

## Decisions

- **`model.Group`**: final identifier, name, ordered member list (`LinkedHashSet`-like semantics enforced by checks), exposing `contains`, `add`, `remove`, read-only `getMembers`. Members are `User` references, so later email changes are reflected automatically.
- **`repository.GroupRepository`** (in-memory, insertion ordered) created by `Park.start` and exposed via `Park.groups()`.
- **`service.GroupService(UserRepository, GroupRepository)`** implements create/add/remove; F2-C04 will add the "no active group ticket" check here (locked state derived, not stored, per the source).
- **User deletion cascades to groups** inside `UserService.deleteUser` (new constructor receiving the group repository); the single-argument constructor keeps an empty group repository for existing tests.
- Group identifiers are compared exactly (case-sensitive).

## Risks / Trade-offs

- [Groups are lost at exit] → expected until F2-C08.
