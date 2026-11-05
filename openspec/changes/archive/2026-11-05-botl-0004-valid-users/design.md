# Design

## Context

`User` (model package) holds the data with no validation. The assignment forbids regular expressions for email and password checks and external libraries for passwords.

## Goals / Non-Goals

**Goals:** a stateless, reusable `UserValidator` in `es.upm.amusementpark.model` with one method per rule and a whole-user check.

**Non-Goals:** error messages/reporting format (decided by the commands in F1-C03), uniqueness checks.

## Decisions

- **Separate validator instead of validating in the `User` constructor**: USER UPDATE (F1-C06) must check a candidate value before applying it, and later deliveries load users from files; a separate validator lets each caller decide when to validate. Alternative (constructor throws) rejected because it couples persistence loading and updates to exceptions.
- **No regex anywhere**: DNI and card use `Character.isDigit` loops; email uses `split("@", -1)` and `split("\\.", -1)` — the latter is a literal-dot split argument, which `String.split` requires; it is used purely as a separator, not as pattern matching of the address. Password scans characters with `Character` predicates.
- **Assumptions recorded** (source silent): the domain part must be `text.xxx` with exactly one dot; the DNI letter is compared case-insensitively; a symbol is any non-letter, non-digit, non-whitespace character; extension characters are not restricted beyond the length of three.

## Risks / Trade-offs

- [`split("\\.")` may be read as a regex] → it is the only way `String.split` accepts a dot separator; email structure is still determined by String operations. Documented here.
