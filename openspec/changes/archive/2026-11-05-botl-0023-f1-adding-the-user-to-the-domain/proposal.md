# Proposal

## Why

Milestone F1-C01 introduces the user as an independent domain object holding the data later features need (first name, surnames, DNI, e-mail, password, phone, card), with proper encapsulation and an immutable DNI. Until now the code base has referred to users only by e-mail through the `UserDirectory` port, because this milestone came late in the batch order.

## What Changes

- Add `business.User`, with private attributes `firstName`, `surnames`, `dni`, `email`, `password`, `phone` and `card`, exposed through getters.
- The DNI is final and has no setter. The other attributes have setters for later management operations (user update).
- The `password` attribute holds the stored (protected) password value, as the `password-protection` capability requires. The class never hashes or stores anything itself.
- `User` has no persistence or I/O responsibility. Application start-up stays in `App`, and storing and retrieving users belongs to a separate repository class (user persistence milestone).
- Required fields are non-null. Business validation of their formats is out of scope here (valid-users milestone).

### Out of scope / deferred
- Validation rules (F1-C02), registration and `USER CREATE` (F1-C03), user persistence (F1-C04), deletion and update commands.
- Replacing e-mail references (tickets, groups, attractions) with `User` object references requires a user registry in central management. That registry is introduced by user registration (F1-C03), which is where this migration is deferred.
- `uml/model.puml` gains the `User` class.

## Capabilities

### New Capabilities
- `users`: The user domain concept: its attributes, encapsulation and identity rules.

### Modified Capabilities
<!-- None -->

## Impact

- New `business.User` with unit tests.
- UML model.
