# Spec Delta

## Purpose

Defines the `USER UPDATE` operation that lets an authenticated user change one of an enumerated set of fields, keeping the DNI immutable and all creation-time restrictions in force.

## ADDED Requirements

### Requirement: USER UPDATE command
The application SHALL accept `USER UPDATE;Email;Password;Field;NewValue`, where `Field` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD`, which modify respectively the first name, surnames, email, password, telephone and card number. On success it SHALL apply the new value and print `USER UPDATED`.

#### Scenario: Name updated
- **GIVEN** a registered user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** `USER UPDATED` is printed and the user's first name is `Ana Maria`

#### Scenario: Email, password, phone and card updated
- **GIVEN** the same user
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, then `USER UPDATE;ana@correo.com;Clave1!;CARD;9999888877776666`, then `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana@correo.com;Nueva2#;EMAIL;ana.nueva@correo.com` are processed
- **THEN** each prints `USER UPDATED` and the user has telephone `699999999`, card `9999888877776666`, password `Nueva2#` and email `ana.nueva@correo.com`

### Requirement: Modifiable fields enumerated
The modifiable fields SHALL be represented by an enumeration containing exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`. A `Field` value outside it, including `DNI`, MUST be rejected with an error message starting with `ERROR` and the user left unchanged.

#### Scenario: DNI cannot be modified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error message starting with `ERROR` is printed and the user's DNI is still `12345678Z`

#### Scenario: Unknown field rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an error message starting with `ERROR` is printed and the user is unchanged

### Requirement: Update requires current credentials
Before any modification, the system SHALL check that a user with the given email exists and that the given password is that user's current password; otherwise the update MUST be rejected with an error message starting with `ERROR` and no user changed.

#### Scenario: Wrong password rejected
- **WHEN** `USER UPDATE;ana@correo.com;Wrong1!;NAME;X` is processed
- **THEN** an error message starting with `ERROR` is printed and the first name is unchanged

#### Scenario: Unknown email rejected
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;NAME;X` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: New values satisfy creation rules
A new email SHALL be valid and not used by another user; a new password SHALL satisfy the password rules; a new card number SHALL contain exactly 16 digits. An update that would introduce a value rejected at creation MUST be rejected with an error message starting with `ERROR`, leaving the user unchanged.

#### Scenario: Invalid new values rejected
- **WHEN** an update sets EMAIL to `ana@correo.es`, or PASSWORD to `clave1!`, or CARD to `123456789012345`
- **THEN** each is rejected with an error message starting with `ERROR` and the user keeps the previous value

#### Scenario: Email used by another user rejected
- **GIVEN** another registered user with email `pedro@correo.com`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana's email is still `ana@correo.com`

### Requirement: Updates are persisted
A successful update SHALL be written to the persisted users file so it survives a restart; a rejected update MUST leave the file unchanged.

#### Scenario: Change survives restart
- **GIVEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` succeeded
- **WHEN** the application is restarted
- **THEN** the loaded user with DNI `12345678Z` has telephone `699999999`

#### Scenario: Wrong element count rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME` is processed
- **THEN** an error message starting with `ERROR` is printed and nothing is changed
