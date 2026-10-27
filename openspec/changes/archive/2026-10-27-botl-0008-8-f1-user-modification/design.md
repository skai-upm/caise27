# Design

## Context

Users are mutable in place (setters for all but the DNI); `UserRepository` saves via `UserFileStore`; `UserValidator` holds creation rules.

## Goals / Non-Goals

**Goals:** USER UPDATE reusing creation validation and uniqueness checks; enum for fields.
**Non-Goals:** HELP/EXIT, file-mode policies.

## Decisions

- **`model/UserField` enum** (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`) with a static `fromText(String)` that loops over `values()` comparing names, returning `null` when unknown (avoids exception-driven control flow of `valueOf`). DNI is absent from the enum, so it cannot be targeted.
- **Validate-then-apply**: the candidate value is fully validated before any setter runs, so a rejected update leaves the user untouched.
- **`UserRepository.update(User)`** simply persists the current state (users are mutated in place; the DNI key never changes).
- Order: format → field known → user by email → password → value validation → uniqueness (EMAIL) → apply → save.
