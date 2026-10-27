# Proposal

## Why

Milestone F1-C02 requires the system to decide whether the data defining a user satisfies the assignment's restrictions for DNI, email address, password and card number, in a way later user operations (creation, update) can reuse.

## What Changes

- Add user data validation rules, implemented with String operations and `split` only (no regular expressions, no external libraries):
  - DNI: eight digits plus a final letter that matches the standard Spanish DNI algorithm.
  - Email: `text@text.xxx` — content before `@`, content after `@`, a domain and an extension of exactly three characters.
  - Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
  - Card number: exactly 16 consecutive digits.
- The telephone number is only stored; it is not validated or transformed.
- Validation lives in a reusable component integrated with the user model, ready for USER CREATE / USER UPDATE.
- Record the mandatory milestone commit `[F1-C02] Add user data validation` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `user-management`: adds the validity rules for DNI, email, password and card number, and states that the telephone is stored without validation.

## Impact

- New `UserValidator` class and tests in `es.upm.amusementpark`.
- No new commands yet; USER CREATE (F1-C03) and USER UPDATE (F1-C06) will apply these checks.
