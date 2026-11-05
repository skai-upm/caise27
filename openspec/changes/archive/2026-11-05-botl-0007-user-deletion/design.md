# Design

## Context

`CommandProcessor → UserService → UserRepository → UserStorage` with full-file rewrite on each change (see user-persistence).

## Goals / Non-Goals

**Goals:** add deletion along the existing path; persistence reuses `storage.save`.

**Non-Goals:** soft delete, authentication sessions.

## Decisions

- **`UserRepository.remove(User)` saves after removing**, mirroring `add`, so every mutation persists the same way.
- **`UserService.deleteUser(email, password)`** follows the source order: find by email → not found error → password check (exact `equals`) → remove. Distinct messages (`user not found`, `incorrect password`) follow the source's separate steps; the source does not require hiding which check failed.
- **Element count exactly 3** for `USER DELETE`, consistent with `USER CREATE` handling.

## Risks / Trade-offs

- [Distinct error messages reveal whether an email is registered] → acceptable in this course context; can be unified later without spec impact beyond messages.
