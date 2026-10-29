# Proposal

## Why

Milestone F1-C05 adds `USER DELETE;Email;Password`: locate the user by email, verify that it exists, check the password, delete it and update persistent data; a wrong password must not allow deletion. Operations from previous milestones must keep working, so the deletion must also respect the rules this batch already specifies for USER DELETE (active tickets and locked groups block it) and keep groups consistent.

## What Changes

- `USER DELETE;Email;Password` (two fields): user must exist; the password is verified against the stored BCrypt value; then the deletion is checked against the existing `tickets` rules (no active ticket, including tickets inside group tickets, and no membership in a group locked by an active group ticket); if allowed, the user is removed from every group it belongs to and from the registered users. Confirmation: `USER DELETED`.
- The deletion is persisted (users and groups files); historical tickets keep the deleted user's email and DNI and are still reconstructed.
- Any failure shows an error and changes nothing.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: user deletion command.

## Impact

- `ParkController.deleteUser`, `CommandProcessor`, tests.

## Ambiguities (flagged)

- Removing the deleted user from its groups comes from the Delivery 2 rules (§9, relationship between users and tickets) already referenced by the `tickets` spec; it is applied here because USER DELETE is introduced now.
- A user currently inside an attraction is not removed from it by deletion (occupancy is temporary execution state; the source is silent).
- Error order follows the source steps: unknown user, then wrong password, then ticket/group restrictions.
