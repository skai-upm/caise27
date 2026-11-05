# Proposal

## Why

Milestone F1-C02 requires the system to decide whether the data defining a user satisfies the assignment's restrictions for DNI, email, password and card number. Upcoming operations (USER CREATE in F1-C03, USER UPDATE in F1-C06) must reuse the same checks, so they are needed as a reusable part of the model before any command exists.

## What Changes

- Add user data validation rules:
  - DNI: eight digits plus a final letter matching the Spanish DNI check-letter algorithm.
  - Email: `text@text.xxx` with content before `@`, a domain, and an extension of exactly three characters, checked with String operations and `split` (no regular expressions).
  - Password: at least one uppercase letter, one lowercase letter, one digit and one symbol (no regular expressions or external libraries).
  - Card number: exactly 16 consecutive digits.
  - Telephone: stored as given, no validation.
- A whole-user check combining the rules, reusable by later operations.
- Out of scope: invoking validation from commands (F1-C03/F1-C06), uniqueness of DNI/email (F1-C03), persistence.

## Capabilities

### New Capabilities
- `user-validation`: Rules that decide whether user data (DNI, email, password, card number) is valid.

### Modified Capabilities
<!-- None: user-domain requirements (attributes, encapsulation, immutable DNI) are unchanged. -->

## Impact

- New validator class in the model package with unit tests. `User` unchanged.
