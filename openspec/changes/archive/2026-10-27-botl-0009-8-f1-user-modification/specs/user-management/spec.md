# Spec Delta

## ADDED Requirements

### Requirement: User update command
The application SHALL accept `USER UPDATE;Email;Password;Field;NewValue` with exactly four data elements and, when all checks pass, change the given field of the user identified by `Email` to `NewValue` and confirm with `USER UPDATED`.

#### Scenario: Update phone
- **WHEN** the user `ana@correo.com` with password `Clave1!` exists and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** `USER UPDATED` is printed and the user's phone is `699999999`

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed for that user
- **THEN** `USER UPDATED` is printed and the user's first name is `Ana Maria`

#### Scenario: Wrong number of elements
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE` is processed
- **THEN** an error describing the expected syntax is printed and nothing changes

### Requirement: Modifiable fields enumeration
The modifiable fields SHALL be represented by an enumeration containing exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`. Any other field, including `DNI`, SHALL be rejected and the DNI SHALL remain unchanged.

#### Scenario: DNI cannot be modified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is printed and the user's DNI is still `12345678Z`

#### Scenario: Unknown field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an error listing the modifiable fields is printed and nothing changes

### Requirement: Identification before update
Before any modification the current email and password SHALL be checked; an unknown email or incorrect password SHALL reject the update.

#### Scenario: Incorrect password
- **WHEN** `USER UPDATE;ana@correo.com;Otra1!;PHONE;699999999` is processed for a user whose password is `Clave1!`
- **THEN** an error is printed and the phone is unchanged

#### Scenario: Unknown user
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;PHONE;699999999` is processed and no such user exists
- **THEN** an error is printed

### Requirement: Updated values keep creation restrictions
A new value SHALL satisfy the same validation rules as user creation: a new email SHALL be valid and not used by another user, a new password SHALL meet the password rules, and a new card number SHALL contain exactly 16 digits. A rejected value SHALL leave the user unchanged.

#### Scenario: Email change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** `USER UPDATED` is printed and the user is identified afterwards by `ana.nueva@correo.com` and not by `ana@correo.com`

#### Scenario: Email used by another user
- **WHEN** `pedro@correo.com` belongs to another user and `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed
- **THEN** an error about the duplicate email is printed and Ana keeps `ana@correo.com`

#### Scenario: Invalid new values
- **WHEN** an update sets EMAIL to `ana@correo.es`, PASSWORD to `clave1!` or CARD to `123456789012345`
- **THEN** each update is rejected with an error and the user is unchanged

#### Scenario: Valid new password and card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` and then `USER UPDATE;ana@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** both print `USER UPDATED` and the user has password `Nueva2#` and card `9999888877776666`

### Requirement: Updates persist
A successful modification SHALL update the persisted users so that it is visible in later executions.

#### Scenario: Update survives restart
- **WHEN** a user's phone is updated to `699999999` and the application is started again
- **THEN** the user's phone is `699999999`
