# Proposal

## Why

Milestone F1-C02 requires the system to decide whether user data satisfies the assignment's restrictions for DNI, email, password and card number, integrated with the model so later user operations (creation, update) reuse the checks. Delivery 3's password protection also requires these password rules to be applied before a new password is stored.

## What Changes

- A user-data validator in the business layer with one check per rule, implemented with basic `String` operations and `split` (no regular expressions, no libraries):
  - DNI: eight digits followed by the letter given by the standard Spanish algorithm (number mod 23 over `TRWAGMYFPDXBNJZSQVHLCKE`);
  - email: `text@text.xxx` — content before `@`, a domain and an extension of exactly three characters after it;
  - password: at least one uppercase letter, one lowercase letter, one digit and one symbol;
  - card: exactly 16 consecutive digits.
- A combined check of a user's data that reports every invalid field, for USER CREATE / USER UPDATE to reuse.
- Setting or changing a password applies the password rules before protecting and storing it.
- The telephone number is stored without validation or transformation.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: validation rules for DNI, email, password and card.
- `password-protection`: the password rules are applied before a new password is stored.

## Impact

- New `business.validation.UserDataValidator`; `ParkController.setPassword`; tests; UML.

## Ambiguities (flagged)

- DNI letter: upper case required (the source examples use upper case).
- Email: exactly one `@`; the extension is the text after the last `.` of the domain part and must be exactly three characters (any characters); the domain name before it must be non-empty, so `ana@mail.correo.com` is valid.
- Password "symbol": any character that is neither a letter, a digit nor whitespace.
- USER CREATE and USER UPDATE come in later proposals; they will call the combined check.
