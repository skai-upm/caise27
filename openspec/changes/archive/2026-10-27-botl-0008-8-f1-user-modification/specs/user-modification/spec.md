# Spec Delta

## Purpose

Defines the `USER UPDATE` command that lets an identified user change authorised fields while preserving all creation-time validation and uniqueness rules.

## ADDED Requirements

### Requirement: USER UPDATE syntax and modifiable fields
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL require exactly four non-empty fields, and Field SHALL be one of the modifiable fields `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`, represented by an enumeration.

#### Scenario: Update the name
- **WHEN** user `ana@correo.com`/`Clave1!` exists and `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the response is `USER UPDATED` and the user's first name is `Ana Maria`

#### Scenario: Unknown field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an error about the field is reported and the user is unchanged

#### Scenario: Wrong number of fields
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE` is processed
- **THEN** a format error is reported

### Requirement: DNI not modifiable
The DNI SHALL NOT be modifiable through USER UPDATE.

#### Scenario: Attempt to change the DNI
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is reported and the user's DNI is still `12345678Z`

### Requirement: Identification before modification
Before any modification the user SHALL be located by the current email and the current password SHALL be checked.

#### Scenario: Unknown email
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** an error indicating the user does not exist is reported

#### Scenario: Incorrect password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999` is processed
- **THEN** an error indicating an incorrect password is reported and the phone is unchanged

### Requirement: New values validated as in creation
New values SHALL satisfy the creation rules: a new email must be valid and not used by another user, a new password must meet the password rules and a new card must contain exactly 16 digits.

#### Scenario: Valid new email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** the response is `USER UPDATED` and the user is now found by `ana.nueva@correo.com`

#### Scenario: Email used by another user
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed
- **THEN** an error about the duplicate email is reported and Ana's email is unchanged

#### Scenario: Invalid new email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es` is processed
- **THEN** an error about the email is reported

#### Scenario: Invalid new password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;nueva2#` is processed
- **THEN** an error about the password is reported and the password is still `Clave1!`

#### Scenario: Valid new password and card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` and then `USER UPDATE;ana@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** both respond `USER UPDATED`

#### Scenario: Invalid new card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;CARD;1234` is processed
- **THEN** an error about the card number is reported

### Requirement: Modifications persisted
A successful modification SHALL update the persistent user information.

#### Scenario: Change survives restart
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` succeeds and the application is restarted
- **THEN** the stored line for DNI `12345678Z` has telephone `699999999`
