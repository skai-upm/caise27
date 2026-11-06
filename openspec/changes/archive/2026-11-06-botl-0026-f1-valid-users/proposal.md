# Proposal

## Why

Milestone F1-C02 requires the system to decide whether user data meets the assignment's restrictions for DNI, e-mail, password and card number. The checks must be reusable by later user operations (registration, update), and the password rules must be applied before a password is protected. The `User` model exists but nothing validates its data.

## What Changes

- Add `business.UserValidator` with reusable checks, built from basic String operations and `split` only (no regular expressions or libraries):
  - DNI: eight digits plus the check letter given by the standard algorithm (`number mod 23` over `TRWAGMYFPDXBNJZSQVHLCKE`).
  - E-mail: `text@text.xxx`, with content before and after `@`, a non-empty domain, and an extension of exactly three characters.
  - Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
  - Card: exactly 16 digits.
- `validate(dni, email, password, card)` throws `ParkException` naming the first violated rule, for reuse by user operations.
- The phone is only stored, with no validation.

### Assumptions
- The DNI letter must be uppercase.
- E-mail addresses may not contain whitespace and must contain exactly one `@`.
- A "symbol" is any character that isn't a letter, digit or whitespace.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `users`: adds the user data validation rules.

## Impact

- New `UserValidator` with tests.
- UML.
