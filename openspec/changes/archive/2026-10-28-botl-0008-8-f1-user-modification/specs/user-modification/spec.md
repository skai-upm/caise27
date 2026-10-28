# Spec Delta

## Purpose

Defines modification of user data through the `USER UPDATE` command: which fields can change, how the user is identified, how new values are validated, and how the change is persisted.

## ADDED Requirements

### Requirement: USER UPDATE command
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL, after locating the user by the current email and checking the password, set the given field to the new value, update the persistent users file and print `USER UPDATED`.

#### Scenario: Update the name
- **WHEN** Ana exists with email `ana@correo.com` and password `Clave1!` and `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** `USER UPDATED` is printed, Ana's first name is `Ana Maria`, and the users file reflects it

#### Scenario: Update the phone
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed for an existing Ana
- **THEN** `USER UPDATED` is printed and Ana's phone is `699999999`

### Requirement: Identification before modification
The current email and password SHALL be checked before any modification; an unknown email or an incorrect password SHALL be rejected with an `ERROR:` message and nothing SHALL change.

#### Scenario: Wrong password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999` is processed and Ana's password is `Clave1!`
- **THEN** an `ERROR:` line about the incorrect password is printed and Ana's phone is unchanged

#### Scenario: Unknown email
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;PHONE;699999999` is processed and no user has that email
- **THEN** an `ERROR:` line reporting the user was not found is printed

### Requirement: Modifiable fields enum
The modifiable fields SHALL be represented by an enum with exactly the values `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`. Any other field name, including `DNI`, SHALL be rejected with an `ERROR:` message and the DNI SHALL never change.

#### Scenario: Attempt to modify the DNI
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an `ERROR:` line reporting a field that cannot be modified is printed and Ana's DNI is unchanged

### Requirement: New values follow creation rules
A new email SHALL be valid and not used by another user, a new password SHALL meet the password rules, and a new card number SHALL contain exactly 16 digits; otherwise the update SHALL be rejected with an `ERROR:` message and nothing SHALL change.

#### Scenario: Email used by another user
- **WHEN** Pedro has email `pedro@correo.com` and `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed
- **THEN** an `ERROR:` line about the email already in use is printed and Ana's email is unchanged

#### Scenario: Invalid new values
- **WHEN** an update sets EMAIL to `ana@correo`, PASSWORD to `clave1!`, or CARD to `123456789012345`
- **THEN** an `ERROR:` line about the invalid value is printed and the user is unchanged

#### Scenario: Valid new email and password take effect
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed and then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`
- **THEN** both print `USER UPDATED` and the user is now identified by `ana.nueva@correo.com` and `Nueva2#`
