# Design

## Context

`model.User` holds the data without checks. See proposal.md for motivation; rules in specs/user-validation.

## Goals / Non-Goals

**Goals:** one reusable place for the four field rules plus a whole-user check.
**Non-Goals:** enforcing validation in the `User` constructor or setters (decided by the operations that come later); error message texts (defined with the commands).

## Decisions

- **`UserValidator` utility class with static boolean methods** `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCardNumber`, `isValid(User)`. Alternative: validate inside `User` and throw — rejected because later commands need to report which rule failed and decide the reaction themselves.
- **No regular expressions anywhere in the validator**, not only where forbidden, for a uniform character-by-character approach. Email uses `split("@", -1)` (limit -1 keeps trailing empty parts, so `ana@` is detected) plus `lastIndexOf('.')`.
- **Null values are invalid** (defensive; avoids NPE in later commands).

## Risks / Trade-offs

- [Interpretations listed in proposal Impact may differ from graders' expectations] → isolated in one class with one test per example from the statement.
