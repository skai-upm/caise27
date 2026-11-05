# user-modification Specification

## Purpose
Defines how an existing user's data is modified with the `USER UPDATE` command: authentication, the closed set of modifiable fields, validation and uniqueness of new values, immutable DNI, and persistence of the change.

## Requirements

### Requirement: USER UPDATE command format
The system SHALL accept the command `USER UPDATE;Email;Password;Field;NewValue` with exactly four data elements after the command name. A `USER UPDATE` with a different number of elements MUST be rejected with an `ERROR` response and no user modified.

#### Scenario: Missing new value
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME` is processed
- **THEN** an `ERROR` response is printed and no user is modified

### Requirement: Authentication before modification
Before any modification, the system SHALL locate the user by the given current email and verify the given password against the user's stored BCrypt-protected password with the jBCrypt library. If no user has that email or the password does not match, the command MUST be rejected with an `ERROR` response and no user modified.

#### Scenario: Incorrect password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;NAME;Ana Maria` is processed for a user whose password is `Clave1!`
- **THEN** an `ERROR` response is printed and the user's name is unchanged

#### Scenario: Unknown email
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;NAME;X` is processed and no user has that email
- **THEN** an `ERROR` response is printed

### Requirement: Modifiable fields
The modifiable fields SHALL be exactly `NAME` (first name), `SURNAME` (surnames), `EMAIL`, `PASSWORD`, `PHONE` (telephone) and `CARD` (card number), represented by an enumeration. Any other field name, including `DNI`, MUST be rejected with an `ERROR` response and no user modified, so that the DNI remains immutable.

#### Scenario: Name updated
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** a response starting with `OK` is printed and the user's first name is `Ana Maria`

#### Scenario: Phone updated without format rules
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;+34 699 000 111` is processed with correct credentials
- **THEN** a response starting with `OK` is printed and the telephone is `+34 699 000 111`

#### Scenario: DNI change rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed with correct credentials
- **THEN** an `ERROR` response is printed and the user's DNI is unchanged

#### Scenario: Unknown field rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an `ERROR` response is printed and the user is unchanged

### Requirement: New values satisfy creation rules
A new email SHALL be valid and not used by another user; a new password SHALL satisfy the password rules; a new card number SHALL contain exactly 16 digits. An update whose new value violates these rules MUST be rejected with an `ERROR` response, leaving the user unchanged.

#### Scenario: Valid new email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed with correct credentials
- **THEN** a response starting with `OK` is printed and the user is now found by `ana.nueva@correo.com`

#### Scenario: Invalid new email
- **WHEN** the new email is `ana@correo.es`
- **THEN** an `ERROR` response is printed and the email is unchanged

#### Scenario: Email used by another user
- **WHEN** another user already has email `luis@correo.com` and Ana updates her `EMAIL` to `luis@correo.com`
- **THEN** an `ERROR` response is printed and Ana's email is unchanged

#### Scenario: Invalid new password
- **WHEN** the new `PASSWORD` is `Clave12`
- **THEN** an `ERROR` response is printed and the password is unchanged

#### Scenario: Valid new password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** a later command authenticating with password `Nueva2#` succeeds and one with `Clave1!` fails

#### Scenario: Invalid new card
- **WHEN** the new `CARD` is `999988887777666`
- **THEN** an `ERROR` response is printed and the card is unchanged

### Requirement: Modifications persisted
A successful update SHALL be written to the users file so that it survives a restart.

#### Scenario: Update survives restart
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;CARD;9999888877776666` succeeds and the application is restarted
- **THEN** the loaded user with DNI `12345678Z` has card number `9999888877776666`
