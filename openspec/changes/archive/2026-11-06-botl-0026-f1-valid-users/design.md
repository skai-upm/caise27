# Design

## Decisions

- **`business.UserValidator`** is a final utility with static predicates (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`) and `validate(dni, email, password, card)`, which throws `ParkException` with a message per rule (`Invalid DNI`, `Invalid email`, `Invalid password: ...`, `Invalid card number`). It lives in business because these are business rules shared by registration and update. A static utility suits stateless rules; no injection is needed.
- **No regex**: all checks use `charAt`, `Character.isDigit/isUpperCase/isLowerCase/isLetterOrDigit/isWhitespace`, `split("@", -1)` and `lastIndexOf('.')`. `TechnicalRestrictionsTest` already forbids regex APIs.
- **DNI**: the length is 9, the first 8 characters are digits, and `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(Integer.parseInt(number) % 23)` must equal the last character.
