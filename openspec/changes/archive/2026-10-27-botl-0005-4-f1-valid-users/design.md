# Design

## Context

`User` and `UserRepository` exist (F1-C01). Delivery 1 forbids regular expressions and external libraries; command processing uses String operations and `split`.

## Goals / Non-Goals

**Goals:**
- One validator with per-field checks plus a whole-user check returning error messages.

**Non-Goals:**
- Enforcing validation in `User` itself or in commands (USER CREATE is F1-C03; it will call the validator).
- Validating names or telephone (not required by the source).

## Decisions

- **`UserValidator`** with static methods `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard` and `validate(dni, email, password, card)` returning `List<String>` error messages (empty when valid). Static helpers keep the checks stateless and reusable; a separate class keeps `User` a plain domain object.
- **No regex anywhere:** `String.split("@", -1)` is a single-character literal split (the String fast path, no regex semantics used); the extension is taken with `lastIndexOf('.')`; character classes via `Character.isDigit/isUpperCase/isLowerCase/isLetterOrDigit` over ASCII digits for DNI/card.
- **Email edge cases (ambiguity, flagged):** exactly one `@` is required; the extension is the text after the last `.` of the part after `@`; the domain is everything between `@` and that `.` and must be non-empty. The source does not say whether a domain may contain extra dots (e.g. `a@b.c.com`); this design accepts it because it still matches `text@text.xxx` with "text" containing a dot.
- **Symbol definition (ambiguity, flagged):** a symbol is any character that is not a letter, digit or whitespace.
- **DNI letter case (ambiguity, flagged):** the control letter must be uppercase, as in the algorithm table and the source example; `12345678z` is rejected.
- **Mandatory commit:** `[F1-C02] Add user data validation` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Assumed edge-case rules may differ from evaluators' expectations] → They are isolated in one class and covered by tests, so they can be adjusted cheaply.
