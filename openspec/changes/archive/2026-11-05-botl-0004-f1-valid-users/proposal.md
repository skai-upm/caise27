# Proposal

## Why

Milestone F1-C02 (Delivery 1, step 4) requires the system to decide whether the data defining a user meets the assignment's restrictions for DNI, email address, password and card number. Later user operations (registration, modification) must reuse these checks. Today `User` (from F1-C01) accepts any values and no validation exists.

## What Changes

- Add user data validation, separate from the `User` data class and reusable by later operations:
  - DNI: eight digits plus a final letter that matches the standard Spanish DNI check-letter algorithm (§5.1).
  - Email: `text@text.xxx` checked with String operations and `split`, without regular expressions: content before `@`, content after `@`, a domain, and an extension of exactly three characters (§5.2).
  - Password: at least one uppercase letter, one lowercase letter, one digit and one symbol, with no regular expressions or external libraries (§5.3).
  - Card number: exactly 16 consecutive digits, with no spaces, letters or symbols (§5.4).
  - Telephone: stored as given, with no validation or transformation (§5.5).
- Add a whole-user check that reports whether a user's data satisfies every rule.

In scope: the validation rules above and their integration point with the model.

Out of scope: rejecting invalid data in commands and user registration (milestone "user registration"), error-message wording for commands, and telephone format rules.

## Capabilities

### New Capabilities
- `user-validation`: Rules that decide whether user data (DNI, email, password, card number) is valid, reusable by user operations.

### Modified Capabilities
- None. The `user-model` requirements (attributes, encapsulation, immutable DNI, no persistence) are unchanged.

## Impact

- New class `es.upm.parque.model.UserValidator` and its tests.
- `User` itself is unchanged; no behavior change to `App` or packaging.
