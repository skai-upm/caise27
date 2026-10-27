# Proposal

## Why

Milestone F1-C01 introduces the user as an independent domain object that retains the data later features (validation, registration, persistence, deletion, modification) will operate on. The executable shell from F0-C02 has no domain yet.

## What Changes

- Add a user domain class holding first name, surname(s), DNI, email address, password, telephone number and card number.
- Encapsulate every attribute (private state, accessors); the DNI is fixed at creation and cannot be changed afterwards.
- Keep the user class free of persistence and I/O so storage and application start-up stay in separate classes (source §3.2).
- Commit with the exact message `[F1-C01] Add user model`.

Out of scope (later milestones): validation rules (F1-C02), registration command (F1-C03), the storage class and file persistence (F1-C04).

## Capabilities

### New Capabilities
- `user-model`: The user entity — its attributes, encapsulation and the immutability of the DNI.

### Modified Capabilities
<!-- none -->

## Impact

- New `src/main/java/es/upm/amusementpark/model/User.java` and its unit test.
- Ambiguity flagged: the source lists attributes but no format rules here; no validation is added in this milestone. Whether other attributes may change is not stated; since §3.1 only forbids changing the DNI, the other attributes get setters to allow the later modification milestone (F1-C06).
