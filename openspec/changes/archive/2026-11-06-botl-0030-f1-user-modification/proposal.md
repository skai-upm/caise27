# Proposal

## Why

Milestone F1-C06 adds `USER UPDATE;Email;Password;Field;NewValue`. Modifiable fields must be represented by an enum (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD), the DNI must stay immutable, and the current email and password must be checked first. New values must satisfy the creation rules (a valid and unused email, a valid password, a 16-digit card), and changes must persist. In this codebase, groups, tickets and attraction occupants refer to users by email. Changing the email must therefore keep those records attached to the same user, or the user would lose their tickets and memberships.

## What Changes

- Add the `UserField` enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) and `USER UPDATE;Email;Password;Field;NewValue`, confirmed with `USER UPDATED`.
- Authenticate with the current email and password before any change.
- Validate the new value with the creation rules:
  - NAME, SURNAME and PHONE must be non-empty.
  - EMAIL must be valid and unused.
  - PASSWORD must be valid, and is stored protected again.
  - CARD must be 16 digits.
- Reject `DNI` and any unknown field without changing anything.
- When the email changes, the user keeps their identity across the park: group memberships, individual ticket owners (including composed tickets, and therefore their passes and invoices) and attraction occupancy move to the new email.
- Store the updated user (and the park state, after an email change) so that changes survive restarts.

### In scope
- Field enum, USER UPDATE, validation, authentication, email-change consistency, persistence, tests.

### Out of scope
- Changing the DNI (forbidden). Replacing email references with object references across the model (the email change keeps references consistent instead).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: adds the USER UPDATE requirement, the field enumeration, and email-change consistency with park records.

## Impact

- New `business.model.UserField`. `UserController.updateUser`. `UserRegistry.rename`. `ParkController.changeUserEmail`, with `Group.replaceMember`, `IndividualTicket.reassignOwner` and `Attraction.replaceOccupant`.
- `CommandProcessor` handler. The catalog/handler test now expects every catalog command to have a handler.
- Tests, UML, README.
