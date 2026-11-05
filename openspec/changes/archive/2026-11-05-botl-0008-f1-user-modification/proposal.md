# Proposal

## Why

Milestone F1-C06 (Delivery 1, step 8) requires the `USER UPDATE` command, so users can change authorized fields after authenticating with their current email and password. Updates must keep the DNI immutable, must not produce a state that creation would have rejected (validation and email uniqueness), and must persist. Today `USER UPDATE` is answered as an unknown command.

## What Changes

- Implement `USER UPDATE;Email;Password;Field;NewValue` (source §9). Before any modification, the current email and password are checked.
- Represent the modifiable fields with an enum: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. Any other field, including `DNI`, is rejected.
- Validate new values with the creation rules: a new email must be valid and not used by another user, a new password must meet the password rules, and a new card must have exactly 16 digits. Name, surname and phone have no format rules, as at creation.
- Persist the modified user set after a successful update.
- Update the command-interface example of a not-yet-implemented command, since `USER UPDATE` is now implemented.

In scope: `USER UPDATE`, the modifiable-field enum, validation and uniqueness on update, persistence.

Out of scope: `HELP`/`EXIT`, file input, changing several fields in one command.

## Capabilities

### New Capabilities
- `user-modification`: The `USER UPDATE` command — authentication by email and password, the enum of modifiable fields, validation and uniqueness of new values, DNI immutability, and persistence.

### Modified Capabilities
- `command-interface`: the "not-yet-supported command" scenario's example changes from `USER UPDATE` to a command name outside the assignment's command set.

## Impact

- New enum `model.UserField`; `CommandProcessor` gains the `USER UPDATE` branch; `UserRepository` gains a way to persist after an in-place change; tests.
