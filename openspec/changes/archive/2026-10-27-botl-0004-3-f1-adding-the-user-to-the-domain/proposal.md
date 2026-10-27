# Proposal

## Why

Milestone F1-C01 introduces the user as an independent domain object that keeps all the data later user-management operations (creation, validation, persistence, deletion, update) need. It also fixes the separation of responsibilities between representing users, running the application and storing/retrieving users.

## What Changes

- Add a `User` domain class with the seven attributes from the assignment: first name, surname(s), DNI, email address, password, telephone number and card number, all encapsulated.
- The DNI is fixed at creation; there is no way to change it afterwards.
- Add a separate class responsible for storing and retrieving users (in memory for now; file persistence arrives with F1-C04), so neither `User` nor the main class handles storage.
- Record the mandatory milestone commit `[F1-C01] Add user model` once verified.

## Capabilities

### New Capabilities
- `user-management`: Users of the amusement park application — their data, invariants, and the operations that create, store, delete and update them.

### Modified Capabilities
<!-- None. -->

## Impact

- New classes in `es.upm.amusementpark`: `User`, `UserRepository`, with unit tests.
- No new commands: USER CREATE/DELETE/UPDATE, data validation and file persistence belong to F1-C02..F1-C06.
