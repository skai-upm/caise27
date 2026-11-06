# Design

## Context

`User` (business.model) has no validation. `PasswordService` takes a `PasswordPolicy` and currently receives `PasswordPolicy.notBlank()` from `AppConfiguration`. The source forbids regular expressions and libraries for these checks and asks for String operations and `split`.

## Goals / Non-Goals

**Goals:** reusable, side-effect-free validation, and password protection that applies the same rule.
**Non-Goals:** uniqueness checks or command handling.

## Decisions

- **`business.validation.UserValidator`** (stateless). It has boolean predicates `isValidDni`, `isValidEmail`, `isValidPassword` and `isValidCard`, usable by any operation. It also has throwing checks `requireValidDni(...)` and friends, plus `validateUser(firstName, surnames, dni, email, rawPassword, phone, card)`, which throw `InvalidInputException("Invalid <field>: …")`. A `passwordPolicy()` method returns a `PasswordPolicy` that wraps the password check.
- **Implementation without regex**:
  - DNI: length 9, `Character` digit checks on chars 0-7, `Integer.parseInt` on them, and the letter lookup.
  - Email: `split("@", -1)` must give 2 parts. The domain part's `lastIndexOf('.')` gives a domain (non-empty) and an extension of length 3. No whitespace anywhere.
  - Password: a single pass with `Character.isUpperCase`/`isLowerCase`/`isDigit`, and a symbol is anything else that is not whitespace.
  - Card: length 16 and all chars between `'0'` and `'9'`.
- **Configuration**: `AppConfiguration` creates `PasswordService(new BCryptPasswordHasher(), UserValidator.passwordPolicy())`.
- The validator lives in the business layer, next to the model it protects, and does not touch persistence or presentation.

## Risks / Trade-offs

- [The email rule rejects valid real-world addresses (for example `.es`)] → This is mandated by the source (`ana@correo.es` must be rejected).
