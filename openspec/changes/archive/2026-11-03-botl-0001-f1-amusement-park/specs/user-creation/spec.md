# Spec Delta

## Purpose

Lets park users be registered through the `USER CREATE` text command while enforcing data validity and DNI/email uniqueness (source F1-C03, sections 4-6).

## ADDED Requirements

### Requirement: Text command interpretation
Commands SHALL be text lines whose elements are separated by `;` and interpreted with basic String operations including `split`, without external libraries; an unrecognized or malformed command SHALL be reported as an error without terminating the application.

#### Scenario: Unknown command reported
- **WHEN** the command `FOO;bar` is processed
- **THEN** an error is reported and the application continues accepting commands

### Requirement: Create user command
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL register a new user with the given values, in that parameter order, when all values are valid and unique, and SHALL report success (for example `USER CREATED`).

#### Scenario: Valid unique user created
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed with no existing users
- **THEN** exactly one user is registered with those values and success is reported

### Requirement: Creation validates data before storing
`USER CREATE` SHALL apply the DNI, email, password and card validation rules before storing the user, and SHALL reject the command without registering anything if any value is invalid or the number of parameters is wrong.

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is processed with DNI `12345678A`
- **THEN** an error is reported and no user is registered

#### Scenario: Wrong parameter count rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error is reported, no user is registered and processing continues

### Requirement: DNI and email uniqueness on creation
`USER CREATE` SHALL check, before creating the user, that no existing user has the same DNI and that no existing user has the same email address, and SHALL reject the command if either is already in use.

#### Scenario: Duplicate DNI rejected
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and a different email
- **THEN** an error is reported and the number of users is unchanged

#### Scenario: Duplicate email rejected
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with a different valid DNI and email `ana@correo.com`
- **THEN** an error is reported and the number of users is unchanged
