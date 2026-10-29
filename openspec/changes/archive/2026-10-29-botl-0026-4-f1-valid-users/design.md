# Design

## Context

`User` holds all attributes; `ParkController.setPassword` protects any non-empty password. No validation exists. Business code throws `InvalidValueException` for invalid values.

## Goals / Non-Goals

**Goals:** the four rules, a combined check, password rules before storing. **Non-Goals:** USER commands, DNI/email uniqueness (registration rules).

## Decisions

- **`business.validation.UserDataValidator`** (stateless, static): `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard` and `validate(dni, email, password, card)` throwing `InvalidValueException("invalid user data: DNI, email")` listing all failing fields in that order.
- **No regular expressions**: character loops with `Character.isDigit/isUpperCase/isLowerCase/isLetterOrDigit/isWhitespace` restricted to ASCII digits for DNI/card; email uses `split("@", -1)` and `lastIndexOf('.')`.
- **DNI**: length 9, first 8 ASCII digits, letter = `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`.
- **`ParkController.setPassword`**: `if (!isValidPassword(plain)) throw new InvalidValueException("password must contain ...")` before hashing.
- **Architecture**: validator lives in business; no I/O.

## Risks / Trade-offs

- [Existing tests set passwords] → they already use rule-compliant passwords (`Clave1!`, `Nueva2#`).
