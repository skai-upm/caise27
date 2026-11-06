# Proposal

## Why

Milestone F1-C02 (Delivery 1, step 4) requires the application to decide whether the data defining a user satisfies the assignment's rules for DNI, email address, password and card number. Today a `User` can be built from any strings, so later user operations (registration, modification, persistence) have no reusable check to rely on.

## What Changes

- Add user-data validation rules for:
  - **DNI**: eight digits followed by a letter that matches the standard Spanish DNI check-letter algorithm (`12345678Z` valid, `12345678A` invalid).
  - **Email**: `text@text.xxx` checked with String operations and `split` only (no regular expressions); content before `@`, a domain after `@`, and an extension of exactly three characters.
  - **Password**: at least one uppercase letter, one lowercase letter, one digit and one symbol, checked without regular expressions or external libraries.
  - **Card number**: exactly 16 consecutive digits; spaces, letters and symbols rejected.
  - **Telephone**: stored as given, with no validation or transformation in this delivery.
- Provide a whole-user check that combines the field rules so later operations can reuse it.
- No change to the existing `User` attributes, encapsulation or immutable DNI.

### In scope
- The validation rules of source sections 5.1–5.5 and their reuse from the user model.

### Out of scope
- Registering, persisting, deleting or modifying users, command parsing and error messages printed by commands (later milestones).
- Uniqueness of DNI/email, stricter rules (e.g. minimum password length, phone format) not required by the assignment.

## Capabilities

### New Capabilities
- `valid-users`: rules that determine whether the data defining a user (DNI, email, password, card number; telephone stored as-is) is valid.

### Modified Capabilities
<!-- None: user-management requirements are unchanged; validation is a separate, reusable capability. -->

## Impact

- New validation code in the `es.upm.park.model` package used by the user model; new unit tests.
- No new dependencies, interfaces or data stores. Build and existing tests must keep passing.
