# Proposal

## Why

Milestone F1-C06 adds `USER UPDATE;Email;Password;Field;NewValue`: after checking the current e-mail and password, one authorised field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD — represented by an enum) is changed, the DNI stays immutable, the new value must satisfy the creation rules (valid and unused e-mail, valid password, 16-digit card), and the change persists between runs.

## What Changes

- Add a modifiable-field enumeration with NAME, SURNAME, EMAIL, PASSWORD, PHONE and CARD; any other field (including DNI) is rejected.
- Add an update operation: verify the user exists and the password matches the stored BCrypt value; validate the new value (e-mail format and uniqueness among other users, password rule with a new protected value, card of exactly 16 digits; names and phone stored as given); apply the change; the e-mail index is updated when the e-mail changes so existing references (groups, tickets, passes) keep pointing to the same user.
- Add the `USER UPDATE` command (four fields) with message `USER UPDATED`; changes persist through the save-after-command mechanism.
- Update the UML model with the enumeration.
- Commit with the exact message `[F1-C06] Implement user update`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `user-management`: adds the USER UPDATE command and modifiable fields.

## Impact

- `UserField` enum, `ParkController.updateUser`, `UserUpdateCommand`, UML, tests.
- **Flagged ambiguities:**
  - Field names are matched exactly in upper case, as listed in the source.
  - Setting the e-mail to the user's current e-mail is accepted (it is not used by *another* user).
  - Empty new values are rejected by the command syntax (all fields are required).
