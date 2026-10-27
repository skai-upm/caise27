# Proposal

## Why

Milestone F1-C05 adds `USER DELETE` so users can be removed, but only when they are identified by their email and correct password; the deletion must also be reflected in the persisted users file while all earlier operations keep working.

## What Changes

- New `USER DELETE;Email;Password` command: locate the user by email, verify it exists, check the password, delete the user and update the users file; prints `USER DELETED` on success.
- Unknown email, wrong password or wrong number of elements are rejected with an error and delete nothing.
- Record the mandatory milestone commit `[F1-C05] Implement user deletion` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `user-management`: adds user deletion identified by email and password.

## Impact

- `UserService.deleteUser`, `CommandProcessor` dispatch for `USER DELETE`.
- Persistence already saves after each repository change (F1-C04), so deletion reaches the file through the repository.
