# Design

## Context

`ParkController` (singleton) holds date and price. Commands are registered in `CommandProcessor` by keyword (text before the first `;`). No user model exists yet; F1-C01..F1-C03 (later in this batch) introduce the full user, validation and `USER CREATE`.

## Goals / Non-Goals

**Goals:** groups with membership commands; derived modifiability hook; minimal user registry.

**Non-Goals:** `GROUP DELETE`, group tickets and their lock rules (F2-C04); user attributes, validation and `USER CREATE` (F1 milestones).

## Decisions

- **Model package `es.upm.etsisi.amusementpark.model`**: `User` (encapsulated `email` only for now) and `Group` (`identifier`, `name`, ordered `List<User>` members). Membership uniqueness is enforced in `Group.addMember` (by e-mail) so the invariant holds independently of the command layer.
- **Registries in `ParkController`**: `Map<String, User>` by e-mail and `Map<String, Group>` by identifier (`LinkedHashMap` for stable listing), with `addUser`, `findUser`, `createGroup`, `findGroup`, `addUserToGroup`, `removeUserFromGroup`. Controller operations throw `IllegalArgumentException` / `IllegalStateException` with user-facing messages; commands translate them into `ERROR: ...` lines.
- **Modifiability**: `ParkController.isGroupModifiable(Group)` computes the state from the group's active tickets and the current date. Since ticket support does not exist yet, it returns `true` (no tickets); F2-C04 will supply the ticket query. No boolean "locked" field anywhere.
- **Commands**: `GroupCreateCommand`, `GroupAddCommand`, `GroupRemoveCommand`, each validating field count and non-empty fields; success messages `GROUP CREATED`, `USER ADDED TO GROUP`, `USER REMOVED FROM GROUP`.
- **Testing reset**: `ParkController.reset()` also clears registries.

## Risks / Trade-offs

- [Minimal `User` precedes the full user model] → F1-C01 will extend the same class; e-mail remains the lookup key used by GROUP commands.
