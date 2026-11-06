# Design

## Context

See proposal.md – Why. The current model is a plain encapsulated `es.upm.park.model.User` (immutable DNI, setters for other fields) with no validation. Later milestones (registration, modification, persistence) must reuse the checks.

## Goals / Non-Goals

**Goals:**
- One reusable place holding the four field rules plus a whole-user check.
- Rules implemented with plain String/char operations (no regex, no libraries), per source 5.2/5.3.

**Non-Goals:**
- Rejecting invalid data in the `User` constructor or setters: the decision of *when* to reject belongs to the operations of later milestones (registration/modification) and their error reporting.

## Decisions

- **Stateless validator in the model package** (`UserValidator` with static methods `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCardNumber`, `isValid(User)`) plus a convenience `User.isValid()` delegating to it. Alternative: validation inside constructor/setters — rejected because it would force an exception policy now and break construction of users from not-yet-validated input in later steps.
- **DNI**: length 9, first 8 chars `'0'..'9'`, last char compared with `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`. Only uppercase letters accepted (the standard representation, matching the source examples).
- **Email**: `split("@", -1)` must give exactly 2 non-empty parts; in the domain part `lastIndexOf('.')` separates a non-empty domain from an extension whose length is exactly 3. `split` on a literal `@` and `lastIndexOf` satisfy "String operations and split" without regular expressions.
- **Password**: single pass over chars using `Character.isUpperCase/isLowerCase/isDigit`; any other non-whitespace char counts as a symbol.
- **Card**: length 16 and every char in `'0'..'9'` (ASCII only, so Unicode digits are rejected).
- **Null input** to any rule returns `false` (no exception).

## Risks / Trade-offs

- [Source does not say whether a lowercase DNI letter is acceptable] → uppercase only, matching the documented format; revisit if a later milestone states otherwise.
- [Source does not define "symbol"] → any non-letter, non-digit, non-whitespace character; documented in the spec.
- [Email extension characters unconstrained] → any 3 characters accepted, as the source only fixes the length.
