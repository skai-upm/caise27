# Proposal

## Why

Milestone F1-C02 requires the system to decide whether the data defining a user meets the assignment's rules for DNI, email address, password and card number. The validation must integrate with the existing model so that later user operations (registration, update) reuse it. The user model and BCrypt password protection already exist, but nothing validates user data. The password policy injected into password protection is still the placeholder "not blank" rule.

## What Changes

- Add user data validation, using only String operations (no regular expressions, no libraries):
  - DNI: eight digits followed by the letter that corresponds to the number according to the Spanish algorithm (number mod 23 into `TRWAGMYFPDXBNJZSQVHLCKE`).
  - Email: `text@text.xxx`, with content before `@`, a domain after it, and an extension of exactly three characters.
  - Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
  - Card number: exactly 16 consecutive digits.
  - Telephone: stored as given, with no validation.
- Report invalid data with an invalid-input error that names the failing field.
- Plug the password rule into password protection, so that every password is validated before it is protected and stored.
- Offer a single operation that validates all the data of a user, for reuse by the user operations.

### In scope
- Validation rules, error reporting, integration with password protection and configuration.

### Out of scope
- USER commands and uniqueness of email or DNI (F1-C03 and later).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: adds the validation requirements for DNI, email, password and card, and the reusable whole-user validation.
- `password-protection`: the Delivery 1 password rules are applied before protection.

## Impact

- New `es.upm.park.business.validation.UserValidator`. `AppConfiguration` uses its password rule as the `PasswordPolicy`.
- Tests, UML, README.
