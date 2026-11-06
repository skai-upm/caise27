# user-modification Specification

## Purpose

Lets an authenticated user change the authorized fields of their data with the `USER UPDATE` command, keeping the DNI immutable, every stored user valid and unique, and the change persisted.

## Requirements

### Requirement: Update user command
The system SHALL accept `USER UPDATE;Email;Password;Field;NewValue`, and when the user is authenticated and the new value is acceptable, set the field to the new value, update the persisted user file and print a confirmation. A command without exactly four fields MUST be rejected with an error and change nothing.

#### Scenario: Name updated and persisted
- **GIVEN** a stored user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** a confirmation is printed, the user's first name is `Ana Maria`, and after restarting the application the first name is still `Ana Maria`

#### Scenario: Email updated
- **GIVEN** a stored user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** the user is found by `ana.nueva@correo.com` and no longer by `ana@correo.com`

#### Scenario: Wrong number of fields
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME` is processed
- **THEN** an error reporting the wrong number of fields is printed and nothing changes

### Requirement: Modifiable fields defined by an enum
Modifiable fields SHALL be exactly `NAME` (first name), `SURNAME` (surnames), `EMAIL`, `PASSWORD`, `PHONE` (telephone) and `CARD` (card number), represented by an enumeration. Any other field name, including `DNI`, MUST be rejected with an error and leave the user unchanged.

#### Scenario: DNI cannot be modified
- **GIVEN** a stored user with DNI `12345678Z`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error reporting that the field cannot be modified is printed and the DNI is still `12345678Z`

#### Scenario: Unknown field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an error reporting that the field cannot be modified is printed and the user is unchanged

### Requirement: Authentication before modification
Before any modification the current email and password SHALL be checked; an email that matches no user or an incorrect password MUST reject the command and change nothing.

#### Scenario: Incorrect password
- **WHEN** `USER UPDATE;ana@correo.com;Otra2#;NAME;Ana Maria` is processed for a user whose password is `Clave1!`
- **THEN** an error reporting an incorrect password is printed and the first name is unchanged

#### Scenario: Unknown email
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;NAME;X` is processed
- **THEN** an error reporting that no user has that email is printed

### Requirement: New values follow creation rules
New values SHALL satisfy the same rules as user creation: a new email MUST be valid and not used by another user, a new password MUST meet the password rules, and a new card number MUST contain exactly 16 digits. A rejected value MUST leave the user and the user file unchanged.

#### Scenario: Invalid new email rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es` is processed
- **THEN** an error naming the email is printed and the email is still `ana@correo.com`

#### Scenario: Email used by another user rejected
- **GIVEN** another stored user with email `pedro@correo.com`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed
- **THEN** an error reporting the duplicate email is printed and both users are unchanged

#### Scenario: Invalid new password or card rejected
- **WHEN** an update sets `PASSWORD` to `clave1!` or `CARD` to `123456789012345`
- **THEN** an error naming the field is printed and the stored value is unchanged
