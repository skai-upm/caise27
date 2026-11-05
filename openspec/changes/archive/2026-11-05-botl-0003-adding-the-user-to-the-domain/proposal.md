# Proposal

## Why

Milestone F1-C01 introduces the user concept of the Amusement Park domain. Later Delivery 1 milestones (validation, USER CREATE/DELETE/UPDATE, persistence) all operate on users, so the system needs an independent, encapsulated user object first. Currently the application only has an entry point and an input session.

## What Changes

- Add a user domain object holding: first name, surname(s), DNI, email address, password, telephone number and card number.
- All attributes are encapsulated (private state, accessed through the object's operations).
- The DNI is fixed at creation and cannot be changed afterwards; the other attributes can be changed through the object so that later milestones (USER UPDATE) can build on it.
- In scope: the user model only. Out of scope: data validation rules (F1-C02), commands (F1-C03+), user storage/persistence (F1-C04).

## Capabilities

### New Capabilities
- `user-domain`: The user entity of the amusement park: its attributes, encapsulation and identity (immutable DNI).

### Modified Capabilities
<!-- None -->

## Impact

- New class in a domain package (`es.upm.amusementpark.model`), with unit tests.
- No change to packaging or the interactive session.
