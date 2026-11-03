# user-updates Specification

## Purpose

Modifies authorised user fields through the `USER UPDATE` command, preserving DNI immutability, validation and uniqueness.

## Requirements

### Requirement: USER UPDATE command
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL, after verifying the current email and password, set the named field to the new value, update persistent storage, and report `USER UPDATED`.

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** the first name becomes `Ana Maria` and the change is persisted

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed and the new email is unused
- **THEN** the user's email becomes `ana.nueva@correo.com` and the change persists across restarts

### Requirement: Enumerated modifiable fields
Only the fields NAME, SURNAME, EMAIL, PASSWORD, PHONE and CARD, defined by an enum, SHALL be modifiable; any other field, including DNI, SHALL be rejected.

#### Scenario: DNI update rejected
- **WHEN** `USER UPDATE` names field `DNI`
- **THEN** an error is reported and the user is unchanged

#### Scenario: Unknown field rejected
- **WHEN** `USER UPDATE` names a field outside the enum
- **THEN** an error is reported and the user is unchanged

### Requirement: Update preserves validation and uniqueness
A new value SHALL satisfy the same rules as user creation: a new email must be valid and unused by another user, a new password must meet the password rules, and a new card must have exactly 16 digits; otherwise the update SHALL be rejected without change.

#### Scenario: Email used by another user
- **WHEN** an update sets EMAIL to another user's email
- **THEN** an error is reported and the user is unchanged

#### Scenario: Invalid card
- **WHEN** an update sets CARD to `1234`
- **THEN** an error is reported and the user is unchanged

#### Scenario: Invalid password
- **WHEN** an update sets PASSWORD to `clave1!`
- **THEN** an error is reported and the user is unchanged

### Requirement: Update requires correct credentials
A `USER UPDATE` with an unknown email or incorrect password SHALL NOT modify any user.

#### Scenario: Wrong password
- **WHEN** `USER UPDATE` uses an incorrect password
- **THEN** an error is reported and the user is unchanged
