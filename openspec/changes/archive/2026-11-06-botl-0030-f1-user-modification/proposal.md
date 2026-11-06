# Proposal

## Why

Milestone F1-C06 adds `USER UPDATE;Email;Password;Field;NewValue`. The modifiable fields must be modeled as an enum (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD), and the DNI must never change. The current e-mail and password must be verified first, new values must pass the creation validation rules, and changes must be persisted.

## What Changes

- Add the enum `business.UserField` (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
- Add `ParkController.updateUser(email, password, field, value)`. It authenticates (the user exists and the password is verified against BCrypt), then applies the field rule:
  - EMAIL: valid and not used by another user.
  - PASSWORD: meets the password rules, then is re-protected with a new BCrypt value.
  - CARD: exactly 16 digits.
  - NAME, SURNAME, PHONE: non-empty.
- An e-mail change also updates every place that refers to the user by e-mail (registry key, group memberships, individual tickets including group components, attraction occupants), so the user keeps their memberships, tickets and passes.
- Add `USER UPDATE;Email;Password;Field;NewValue`, which prints `USER UPDATED`. `DNI` as a field is rejected with "DNI cannot be modified", and unknown fields are rejected. Persistence is updated by the after-command save.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `users`: adds user modification.

## Impact

- New `UserField` enum.
- `ParkController.updateUser`, with reference re-keying helpers in `Group`, `IndividualTicket` and `Attraction`.
- `UserCommands`, UML and tests.
