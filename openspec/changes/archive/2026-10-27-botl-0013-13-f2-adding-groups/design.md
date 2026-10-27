# Design

## Context

`ParkController` (singleton) centralises users; groups must be added there. Source §16 (Delivery 2) requires object relationships in memory, not identifiers: groups hold `User` references; users do not know their groups.

## Goals / Non-Goals

**Goals:** group entity, repository, commands.
**Non-Goals:** ticket-based locking (F2-C04), group persistence (F2-C08).

## Decisions

- **`model/Group`**: `final String id`, `String name`, `List<User> members` (insertion order). `addMember` returns false when already present (identity by DNI via `User.equals`); `removeMember` returns false when absent; `getMembers` returns an unmodifiable view.
- **`repository/GroupRepository`**: in-memory `LinkedHashMap<String, Group>` with add/find/remove/findAll, mirroring `UserRepository`.
- **`ParkController.start(...)`** also resets groups to a fresh repository for a new execution.
- **User deletion cascade**: `CommandProcessor.deleteUser` asks the controller to remove the user from all groups before deleting it (keeps the "existing users" invariant; users hold no group references so nothing else changes).
