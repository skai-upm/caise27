# Proposal

## Why

Milestone F1-C02 requires the system to decide whether the data defining a user satisfies the assignment's restrictions on DNI, email address, password and card number, in a way later user operations (registration, modification) can reuse.

## What Changes

- Add reusable validation of user data: DNI (8 digits + correct control letter), email (`text@text.xxx`, String operations and `split`, no regular expressions), password (uppercase, lowercase, digit and symbol, no regular expressions or external libraries), card number (exactly 16 digits).
- Allow checking a whole `User` at once, so later operations can reject invalid users.
- Telephone number remains stored as given, with no validation or transformation.
- Commit with the mandatory message `[F1-C02] Add user data validation`.

## Capabilities

### New Capabilities
- `user-validation`: Rules that determine whether DNI, email, password and card number values are valid, and whether a user's data as a whole is valid.

### Modified Capabilities
- None (the `user-model` requirements are unchanged; validation is offered alongside it).

## Impact

- New `es.upm.amusementpark.model.UserValidator` and tests.
- Ambiguities flagged:
  - DNI control letter: only uppercase letters are accepted (the standard table is uppercase); the source does not say whether lowercase is acceptable.
  - Password "symbol": interpreted as any character that is neither a letter, a digit nor whitespace.
  - Email extension: exactly three characters after the last `.` of the domain part; the source does not restrict which characters.
  - Where validation is enforced (creation vs. registration) is not stated for this milestone; the existing `User` constructor is not changed, and registration (F1-C03) will apply the checks.
