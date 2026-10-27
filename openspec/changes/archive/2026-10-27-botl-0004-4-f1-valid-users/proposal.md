# Proposal

## Why

Milestone F1-C02 requires the system to decide whether the data defining a user meets the assignment's restrictions (DNI, email address, password, card number), in a reusable form so that registration and modification can apply the same checks later.

## What Changes

- Add user-data validation rules for DNI (8 digits + check letter of the Spanish algorithm), email (`text@text.xxx`), password (uppercase, lowercase, digit, symbol) and card number (exactly 16 digits).
- Telephone number has no validation rule; it is only stored (source §5.5).
- Implement with basic `String`/`char` operations only — no regular expressions or external libraries.
- Expose the rules in one validator integrated with the user model so later operations (USER CREATE / USER UPDATE) reuse them.
- Commit with the exact message `[F1-C02] Add user data validation`.

## Capabilities

### New Capabilities
- `user-validation`: The validity rules for user data (DNI, email, password, card number) and their implementation constraints.

### Modified Capabilities
<!-- none -->

## Impact

- New `model/UserValidator` plus unit tests. `User` is unchanged.
- Ambiguities flagged (decided conservatively, not as new business rules):
  - DNI letter is accepted only in uppercase (the source example uses uppercase).
  - "Symbol" in a password means any character that is not a letter, digit or whitespace.
  - Email: exactly one `@`; the text after `@` must contain a dot; the last dot separates domain and extension; the extension must have exactly three characters (character class not specified, so any non-dot characters are accepted).
