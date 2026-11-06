# Design

## Context

`UserController` creates and deletes users, with injected `UserRegistry` (keyed by email), `PasswordService`, `UserStorage` and `ParkController`. `UserValidator` holds the creation rules. Park records reference users by email: `Group.memberEmails`, `IndividualTicket.ownerEmail` (final) and `Attraction.occupantEmails`. Passes reach users through their ticket.

## Goals / Non-Goals

**Goals:** USER UPDATE with full validation, and email changes that keep every park record attached to the same user.
**Non-Goals:** DNI changes, and an object-reference refactor of the model.

## Decisions

- **`business.model.UserField` enum** with `parse(text)`. `DNI` gets a dedicated message ("The DNI cannot be modified"), and unknown names give `InvalidInputException`.
- **`UserController.updateUser(email, rawPassword, field, value)`**:
  1. Authenticate (not found, or wrong password).
  2. Validate the value for the field with `UserValidator`. EMAIL also requires the address to be unused by another user.
  3. Apply:
     - NAME/SURNAME/PHONE/CARD use the setters.
     - PASSWORD uses `setPassword(passwords.protect(value))`.
     - EMAIL calls `registry.rename(old, new)` (re-keys and sets the email), then `park.changeUserEmail(old, new)`.
  4. `save()` the users.
  Every check happens in steps 1-2, before any change.
- **`ParkController.changeUserEmail(old, new)`** calls `Group.replaceMember` (keeps the member order), `IndividualTicket.reassignOwner` (the owner field becomes non-final, with an explicit operation), and `Attraction.replaceOccupant`, then saves the park state. Passes and invoices follow automatically because they read the ticket owner.
- **Command**: `withArgs(args, 4, …)` → `USER UPDATED`.

## Risks / Trade-offs

- [`ownerEmail` becomes mutable] → It is changed only through `reassignOwner`, used only by the email-change operation, and covered by tests.
