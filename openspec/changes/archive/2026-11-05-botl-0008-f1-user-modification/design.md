# Design

## Context

`CommandProcessor` handles `USER CREATE` and `USER DELETE` (auth by email + password). `UserRepository` is DNI-keyed and persists through `UserFileStore` after `add`/`remove`. `User` has setters for all attributes except the DNI. `UserValidator` provides per-field checks.

## Goals / Non-Goals

**Goals:**
- One enum-driven update path that cannot touch the DNI.

**Non-Goals:**
- Multi-field updates and audit history.

## Decisions

- **`model.UserField` enum** `{NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD}`. The field element is parsed with an explicit lookup that returns `null` for unknown names, including `DNI`, rather than `valueOf`, which throws. Because `DNI` is not an enum constant and `User` has no DNI setter, DNI immutability is enforced both by the type and by the API.
- **Field-specific validation via a `switch` on the enum.** EMAIL is validated, then checked for uniqueness against *other* users: setting the email to the user's own current address is accepted as a no-op change. PASSWORD and CARD are validated. NAME, SURNAME and PHONE are accepted as given, matching creation, which applies no rules to them.
- **`UserRepository.update(User)`** persists the current set after an in-place mutation. Keying by DNI means an email change needs no re-indexing.
- **Check order**: element count → authenticate (email lookup, password) → field name → new-value validation and uniqueness → apply setter → persist → `OK: user <dni> updated`.

## Risks / Trade-offs

- [Mutating the `User` before persisting means an I/O failure leaves memory changed but not the file] → Same accepted trade-off as for create and delete. It is reported as `ERROR` by the generic handler.
