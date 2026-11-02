# Spec Delta

## Purpose

Registers new users from the `USER CREATE` command after validating all data and guaranteeing DNI and email uniqueness.

## ADDED Requirements

### Requirement: USER CREATE command
The system SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, with elements separated by `;` and interpreted using basic `String` operations and `split`, and SHALL register exactly one new user when all data is valid and unique, reporting `USER CREATED`.

#### Scenario: Valid unique user is created
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed with no existing users
- **THEN** exactly one user is registered with those values and `USER CREATED` is reported

### Requirement: Validation before storage
User data SHALL be validated with the DNI, email, password and card-number rules before the user is stored; invalid data SHALL be rejected without registering a user.

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is processed with an invalid DNI, email, password or card number
- **THEN** no user is registered, an error is reported and processing continues with the next command

### Requirement: DNI and email uniqueness
Two users SHALL NOT share the same DNI or the same email address; both conditions SHALL be checked before a user is created.

#### Scenario: Duplicate DNI rejected
- **WHEN** `USER CREATE` is processed with a DNI already registered
- **THEN** no additional user is registered and an error is reported

#### Scenario: Duplicate email rejected
- **WHEN** `USER CREATE` is processed with an email already registered
- **THEN** no additional user is registered and an error is reported

### Requirement: Malformed commands do not terminate the application
An unrecognised command, or a recognised command with the wrong number of elements, SHALL be rejected with an error message without terminating the application or changing state.

#### Scenario: Wrong element count
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** no user is registered, an error is reported and the application keeps accepting commands

#### Scenario: Unknown command
- **WHEN** `FOO BAR` is processed
- **THEN** an error is reported and the application keeps accepting commands
