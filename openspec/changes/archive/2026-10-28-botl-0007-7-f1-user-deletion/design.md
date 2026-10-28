# Design

## Context

`UserService` validates/creates and persists via `UserStorage` before updating `UserRepository`. See proposal.md.

## Goals / Non-Goals

**Goals:** deletion following the five mandated steps; reusable email+password identification for `USER UPDATE` (F1-C06).
**Non-Goals:** other commands.

## Decisions

- **`UserService.authenticate(email, password)`** (private helper) returns the user or throws `UserOperationException` (`User not found` / `Incorrect password`); reused later by update.
- **`UserService.deleteUser`**: authenticate → build list without the user → `persist` → `repository.delete(dni)`, so failed saves leave memory unchanged (same rule as creation).
- **`UserRepository.delete(String dni)`** removes by DNI.
- **Processor**: `USER DELETE` requires exactly 2 arguments.

## Risks / Trade-offs

- [Password compared in plain text] → matches the assignment (no encryption in this delivery).
