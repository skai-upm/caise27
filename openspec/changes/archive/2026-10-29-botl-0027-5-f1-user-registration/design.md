# Design

## Context

`UserDataValidator.validate` checks DNI/email/password/card; `ParkController` registers users in an email-keyed map and protects passwords with `PasswordProtector`; `CommandProcessor` lists `USER CREATE` in HELP but reports it as unknown.

## Goals / Non-Goals

**Goals:** USER CREATE with validation and uniqueness. **Non-Goals:** USER DELETE / UPDATE (next proposals).

## Decisions

- **`ParkController.createUser(firstName, surnames, dni, email, password, phone, card)`**: trim fields; `UserDataValidator.validate(dni, email, password, card)` (→ `InvalidValueException`); duplicate email (map lookup) or DNI (traversal) → `DuplicateElementException`; build `User` with `passwords.protect(password)`; `registerUser`. Cooperation: presentation parses, business validates and creates, persistence stores via the existing after-command save.
- **`CommandProcessor`**: case `"USER CREATE"` with 7 arguments → `USER CREATED`.

## Risks / Trade-offs

- [BCrypt cost per creation] → acceptable for interactive and file use.
