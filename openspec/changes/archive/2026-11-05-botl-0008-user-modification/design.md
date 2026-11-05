# Design

## Context

`User` has setters for all attributes except DNI; `UserValidator` holds the creation rules; repository mutations persist through `UserStorage.save`.

## Goals / Non-Goals

**Goals:** enum-driven updates that reuse validation and the existing persistence path.

**Non-Goals:** multi-field updates in one command.

## Decisions

- **`model.UserField` enum** (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`), parsed with a lookup that returns empty for unknown names instead of letting `valueOf` throw; `DNI` is deliberately absent, so it is rejected like any unknown field (with a specific "DNI cannot be modified" message for clarity).
- **Validate before mutating**: `UserService.updateUser` authenticates, validates the candidate value (email: format + not used by *another* user — keeping one's own email is allowed), then applies it via the setter and calls `UserRepository.update(user)` which saves. A failed check throws before any setter runs, so state is unchanged.
- NAME/SURNAME/PHONE have no validation rules in the source, so any value is accepted.

## Risks / Trade-offs

- [Field names are case-sensitive] → consistent with command keywords; the source uses uppercase.
