# Proposal

## Why

Milestone F1-C05 adds `USER DELETE;Email;Password`. The application must locate the user by email, verify that the user exists, check the password, delete the user and update persistent data. An incorrect password must never delete a user. Later deliveries added constraints on deleting users: no active tickets, no locked group, and removal from groups when deletion is allowed. These are already implemented in park management, waiting for this command.

## What Changes

- Add `USER DELETE;Email;Password`. It finds the user by email (error if none), verifies the password against the stored protected value (error if wrong), and applies the park's user-deletion rules. Those rules reject deletion while the user has an active individual or composed ticket or belongs to a locked group, and otherwise remove the user from their groups. Finally it removes the user, updates stored data, and confirms with `USER DELETED`.
- Rejected deletions change neither the user, nor groups, nor stored data. Historical tickets of a deleted user are kept.
- The user management controller collaborates with park management through injection.

### In scope
- USER DELETE, password verification, integration with the park deletion rules, persistence, tests.

### Out of scope
- USER UPDATE (F1-C06).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: adds the USER DELETE requirement.

## Impact

- `UserController.deleteUser` and injection of `ParkController`. `CommandProcessor` handler. `AppConfiguration` and test-support wiring. The catalog/handler test's pending set becomes USER UPDATE only.
- Tests, UML, README.
