# Proposal

## Why

Milestone F1-C03 makes the application accept `USER CREATE` to add new users. Creation must satisfy every user-data restriction and keep DNI and email unique. The user model, user registry, user-data validation and BCrypt password protection exist, but no command creates users. This is also why groups, tickets and invoices can only be exercised with users registered by tests.

## What Changes

- Add `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`. It validates all the data (DNI, email, password, card; non-empty names and phone), rejects a DNI or email already used by another user, protects the password with BCrypt, stores the user, and confirms with `USER CREATED`.
- Any invalid, duplicate, incomplete or malformed request is reported with an error, adds no user, and does not stop command processing.
- Add a user management controller (single instance) that coordinates validation, uniqueness, password protection and storage. It receives the user store and password protection by injection.

### In scope
- USER CREATE, uniqueness, controller, wiring, tests.

### Out of scope
- Persisting users in files (F1-C04), USER DELETE (F1-C05) and USER UPDATE (F1-C06).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: adds the USER CREATE requirement and the uniqueness of DNI and email.

## Impact

- New `business.controller.UserController`. `CommandProcessor` gets the USER CREATE handler and constructor injection of `UserController`. `AppConfiguration` wires it.
- Tests (including the catalog/handler check, where only USER DELETE and USER UPDATE remain pending), UML and README.
