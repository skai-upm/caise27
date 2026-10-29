# Proposal

## Why

Milestone F1-C06 adds `USER UPDATE;Email;Password;Field;NewValue`: authorized fields, represented by an enum, can be changed after checking the current email and password, the DNI stays immutable, new values must pass the same validation and uniqueness rules as user creation, and changes must persist between runs.

## What Changes

- `UserField` enum with the modifiable fields `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (no DNI).
- `USER UPDATE;Email;Password;Field;NewValue` (four fields): locate the user and verify the password first; then apply the change:
  - `EMAIL`: valid format and not used by another user; the user keeps its tickets, groups and history;
  - `PASSWORD`: password rules; a new BCrypt value is stored;
  - `CARD`: exactly 16 digits;
  - `NAME`, `SURNAME`, `PHONE`: stored as given.
- Unknown fields (including `DNI`) and invalid values are rejected with an error and change nothing. Confirmation: `USER UPDATED`. Changes persist through the existing save after each command.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: user modification command and its rules.

## Impact

- `business.model.UserField`, `ParkController.updateUser`, `CommandProcessor`, tests, UML.

## Ambiguities (flagged)

- Field names are matched exactly in upper case, as in the source.
- `NAME`, `SURNAME` and `PHONE` have no validation rules in the source (as at creation), so any value is accepted; an empty value is rejected as unprocessable input.
- Changing the email to the user's own current email is accepted (no other user uses it).
