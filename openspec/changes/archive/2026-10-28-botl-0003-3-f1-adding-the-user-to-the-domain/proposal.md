# Proposal

## Why

Milestone F1-C01 introduces the user as an independent domain object holding the data that later Delivery 1 features (validation, registration, persistence, deletion, modification) operate on. Without it no user management can be built.

## What Changes

- Add a user domain object holding first name, surname(s), DNI, email address, password, telephone number and card number.
- Encapsulate all attributes (private state, accessors); the DNI is fixed at creation and cannot be changed afterwards; the other attributes can be updated through the object's interface so later modification features can be added.
- Keep the user object free of persistence and application-startup logic (separation of responsibilities).
- Commit with the mandatory message `[F1-C01] Add user model`.

## Capabilities

### New Capabilities
- `user-model`: The user concept — which data a user holds, how it is encapsulated, and the immutability of the DNI.

### Modified Capabilities
- None.

## Impact

- New `es.upm.amusementpark.model.User` class and tests. No change to execution modes or the build.
- Ambiguities flagged: the source does not define validation rules here (milestone F1-C02 does) nor whether a user may be created with missing data; this change does not validate values. Section 3.2 also requires a class that stores and retrieves users; that belongs to the registration/persistence milestones (F1-C03/F1-C04) and is not created here, but the user class is designed so it never manages persistence.
