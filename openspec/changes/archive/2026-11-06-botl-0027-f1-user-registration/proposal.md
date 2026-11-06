# Proposal

## Why

Milestone F1-C03 requires the `USER CREATE` command to add new users to the system. Each new user must pass every previously defined restriction (DNI, e-mail, password and card rules), and DNI and e-mail must stay unique. Today the `User` model and `UserValidator` exist, but there is no registry and no command, so the park cannot resolve any user at runtime.

## What Changes

- Add a user registry to central management. `registerUser(firstName, surnames, dni, email, password, phone, card)` does, in order:
  1. Validates the data with `UserValidator`.
  2. Rejects a DNI or e-mail that already exists.
  3. Protects the password with the injected `PasswordHasher` (BCrypt). Only the protected value is kept, per `password-protection`.
  4. Creates and stores the `User`.
- The default `UserDirectory` of central management is now backed by this registry (exists, DNI lookup). Groups, tickets, invoices and access therefore work with real registered users. Injected directories remain possible for tests.
- Add `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` (8 `;`-separated fields, parsed with `split`), which prints `USER CREATED`. Invalid, duplicate or malformed requests print an error and add no user. Command processing never terminates on bad input.
- `reset()` clears the registry. Users are kept in memory only until the user persistence milestone, which comes later in this batch.

### Known gap (recorded, not silently resolved)
- Tickets, groups and attractions still reference users by e-mail, resolved through the registry. The Delivery 2 restriction prefers object references. Migrating those relationships to `User` objects is not part of this milestone's scope and remains open.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `users`: adds user registration through `USER CREATE` and DNI and e-mail uniqueness.

## Impact

- `ParkController` (registry, default directory).
- New `presentation.UserCommands` and its wiring in `App.registerCommands`.
- UML and tests.
