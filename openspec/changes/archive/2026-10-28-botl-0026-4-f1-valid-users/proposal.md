# Proposal

## Why

Milestone F1-C02 requires the system to decide whether the data defining a user meets the assignment's restrictions — DNI, e-mail address, password and card number — in a way integrated with the model so that later user operations (registration, modification) reuse the same checks. The password rule already exists (F3-C05) but DNI, e-mail and card checks do not.

## What Changes

- Add a user data validator in the business layer with individual checks and a whole-user validation:
  - DNI: eight digits followed by the letter given by the standard Spanish algorithm (`TRWAGMYFPDXBNJZSQVHLCKE`[number mod 23]).
  - E-mail: `text@text.xxx` checked with `String` operations and `split` (no regular expressions): exactly one `@`, content before it, a domain, and an extension of exactly three characters after the last dot.
  - Password: at least one uppercase letter, one lowercase letter, one digit and one symbol (reusing the existing password rule).
  - Card: exactly 16 consecutive digits.
  - Telephone: stored as given, no validation.
- Invalid data raises an `InvalidValueException` naming the field.
- Commit with the exact message `[F1-C02] Add user data validation`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `user-management`: adds the DNI, e-mail, password and card validation rules.

## Impact

- New `UserValidator` (business), tests. No command uses it yet: `USER CREATE` (F1-C03) and `USER UPDATE` (F1-C06) follow in this batch.
- **Flagged ambiguities:**
  - The DNI letter must be upper case (the source example uses upper case; lower-case letters are rejected).
  - "Extension of exactly three characters" is checked on length only; the domain may contain further dots before the extension (e.g. `ana@mail.correo.com`).
  - No rule is given for first name, surnames or telephone format; they are not validated beyond the non-empty fields required by the command syntax.
