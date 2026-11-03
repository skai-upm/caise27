# Spec Delta

## Purpose

Registers new users from the `USER CREATE` command, enforcing all validation rules and DNI/email uniqueness.

## ADDED Requirements

### Requirement: USER CREATE command
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL register a new user when all data is valid and both DNI and email are unused, and SHALL report `USER CREATED`.

#### Scenario: Valid unique user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** exactly one user is registered and `USER CREATED` is reported

### Requirement: Uniqueness of DNI and email
The system SHALL reject a `USER CREATE` whose DNI or email already belongs to an existing user, checking both before creating the user.

#### Scenario: Duplicate DNI
- **WHEN** a `USER CREATE` repeats an existing user's DNI with a new email
- **THEN** no user is added and an error is reported

#### Scenario: Duplicate email
- **WHEN** a `USER CREATE` repeats an existing user's email with a new DNI
- **THEN** no user is added and an error is reported

### Requirement: Rejection of invalid creation data
A `USER CREATE` with invalid DNI, email, password or card, or with a wrong number of fields, SHALL be rejected with an error, register no user, and leave the application running.

#### Scenario: Invalid data
- **WHEN** a `USER CREATE` contains an invalid card number
- **THEN** no user is registered, an error is reported and the next command is processed

#### Scenario: Malformed command
- **WHEN** `USER CREATE;Ana` is processed
- **THEN** an error is reported and no user is registered

### Requirement: Command parsing with String operations
Commands SHALL be split on the `;` character using basic String operations including `split`, without external libraries; an unrecognised command SHALL be reported as an error without terminating the application.

#### Scenario: Unknown command
- **WHEN** `FOO BAR` is processed
- **THEN** an error is reported and processing continues
