# Design

## Context

`es.upm.parque.model.User` holds seven String attributes with no validation (see `openspec/specs/user-model`). Source §5.2 and §5.3 forbid regular expressions (and, for passwords, external libraries).

## Goals / Non-Goals

**Goals:**
- Stateless, reusable validation methods callable by future registration/modification code.

**Non-Goals:**
- Enforcing validation inside the `User` constructor/setters. Rejection behaviour and error reporting belong to the operations milestones.

## Decisions

- **Separate `UserValidator` class with static boolean methods** (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCardNumber`, `isValid(User)`), kept in the model package. Putting checks into `User` setters would make every `User` construction throw. Later persistence loading and the immutable-DNI design would then become awkward, and the source only asks that the system "determine whether" data is valid. Alternative: throwing exceptions from `User`. Rejected for now; registration can still decide to reject.
- **Character-by-character checks** using `Character.isDigit`, `isUpperCase`, `isLowerCase` and `isLetter` over ASCII. Email uses `split("@", -1)` and the last `.` of the domain part. No regex APIs (`matches`, `Pattern`) are used anywhere in the validator.
- **DNI letter comparison is case-sensitive (uppercase only).** The source shows uppercase examples and does not mention lowercase, so accepting only uppercase is the conservative reading. This is recorded as an assumption.
- **Email extension = text after the last dot of the part after `@`**, which must be exactly 3 characters. The domain is the part before that dot and must be non-empty. Multi-level domains such as `a@b.c.com` are accepted. The source does not forbid them.
- **"Symbol"** = a character that is not a letter, digit or whitespace.
- **Null inputs are invalid** rather than throwing.

## Risks / Trade-offs

- [Uppercase-only DNI letter may reject `12345678z`] → Documented assumption; trivial to relax if the assignment clarifies.
- [Non-ASCII digits (e.g. Arabic-Indic) pass `Character.isDigit`] → Restrict digit checks to `'0'..'9'`.
