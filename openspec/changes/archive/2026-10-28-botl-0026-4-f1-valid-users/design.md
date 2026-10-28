# Design

## Context

`business.security.PasswordPolicy.validate` implements the password rule and throws `InvalidValueException`. Delivery 1 forbids regular expressions; `split` is allowed.

## Decisions

- **`business.model.UserValidator`** (final utility class): boolean checks `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, and `validate(dni, email, password, card)` throwing `InvalidValueException("Invalid DNI: ...")` etc. Placed in the business layer next to the model so registration/update operations in the controller reuse it.
- **DNI**: length 9, first eight chars `'0'..'9'`, last char equals `LETTERS.charAt(Integer.parseInt(digits) % 23)`.
- **E-mail**: `email.split("@", -1)` must give two non-empty parts; in the domain part `lastIndexOf('.')` must be > 0 (non-empty domain) and the remainder length exactly 3.
- **Password**: `isValidPassword` delegates to `PasswordPolicy.validate` (single source of the rule).
- **Card**: length 16 and every char `'0'..'9'`.
