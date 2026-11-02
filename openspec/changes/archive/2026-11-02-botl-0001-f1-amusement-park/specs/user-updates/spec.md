# Spec Delta

## Purpose

Lets an authenticated user change permitted fields with `USER UPDATE`, without ever reaching a state that creation would have rejected and without changing the DNI.

## ADDED Requirements

### Requirement: USER UPDATE command
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL first verify that a user with the current email exists and that the password matches, then set the named field to the new value and update the persistent information; on success the application reports `USER UPDATED`. (F1-C06)

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` runs with correct credentials
- **THEN** Ana's first name becomes `Ana Maria`, the change persists across restarts and `USER UPDATED` is reported

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` runs with correct credentials and the new email is unused
- **THEN** the user's email becomes `ana.nueva@correo.com` and is persisted

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` runs with an unknown email or an incorrect password
- **THEN** the user remains unchanged and an error is reported

### Requirement: Enum-defined modifiable fields
The modifiable fields SHALL be exactly NAME, SURNAME, EMAIL, PASSWORD, PHONE and CARD, represented by an enum; any other field name, including DNI, SHALL be rejected. (F1-C06)

#### Scenario: Attempt to update DNI
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` runs
- **THEN** the update is rejected and the DNI is unchanged

#### Scenario: Unknown field
- **WHEN** `USER UPDATE` names a field that is not in the enum
- **THEN** the update is rejected and the user is unchanged

### Requirement: Updated values satisfy creation rules
A new value SHALL satisfy the same validation and uniqueness rules as user creation: a new email must be valid and not used by another user, a new password must meet the password rules, a new card must have exactly 16 digits, and no new value may be blank. (F1-C06)

#### Scenario: Email used by another user
- **WHEN** Ana tries to change her email to `pedro@correo.com`, which Pedro already uses
- **THEN** the update is rejected and Ana is unchanged

#### Scenario: Invalid card or password
- **WHEN** an update sets CARD to `1234` or PASSWORD to `clave1!`
- **THEN** the update is rejected and the user is unchanged
