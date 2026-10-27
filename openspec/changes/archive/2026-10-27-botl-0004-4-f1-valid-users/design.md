# Design

## Context

`model/User` holds unvalidated strings. Later commands (F1-C03 create, F1-C06 update) must reuse the checks.

## Goals / Non-Goals

**Goals:** pure, reusable validation functions; no regex.
**Non-Goals:** uniqueness of DNI/email (registration milestone), error messages for commands.

## Decisions

- **`UserValidator` utility class with static predicates** `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, plus `isValid(User)` combining them. Keeping `User` a plain data holder lets update operations validate a candidate value before applying it (alternative: validate inside setters — rejected because it would throw from the model and mix concerns).
- **Character scans with `Character.isDigit/isUpperCase/isLowerCase/isLetterOrDigit/isWhitespace`**, `indexOf`/`lastIndexOf`/`substring` for email. `String.split` is avoided for `.` since its argument is a regex.
- **DNI letter**: `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`; digits restricted to ASCII `0-9` so `Integer.parseInt` is safe.
- **Null inputs** are invalid (never throw).

## Risks / Trade-offs

- [Unicode digits/letters] → digits restricted to ASCII `'0'..'9'` for DNI and card.
