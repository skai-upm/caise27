# Design

## Context

`UserRepository` (persisting via `UserFileStore` after each change) and `CommandProcessor` (USER CREATE) exist.

## Goals / Non-Goals

**Goals:** USER DELETE following the five source steps in order.
**Non-Goals:** USER UPDATE (F1-C06).

## Decisions

- **`UserRepository.remove(User)`** removes by DNI and calls the existing `save()`; persistence errors surface through the existing `PersistenceException` handling.
- **`CommandProcessor.deleteUser`**: format check → `findByEmail` → null → `ERROR: User not found` → `password.equals` → `ERROR: Incorrect password` → remove → `USER DELETED`.
