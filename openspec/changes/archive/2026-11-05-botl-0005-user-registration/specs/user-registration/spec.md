# Spec Delta

## Purpose

Defines the `USER CREATE` operation that registers a new user after validating its data and checking that its DNI and email address are not already in use.

## ADDED Requirements

### Requirement: USER CREATE command
The application SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and, when the data is valid and not duplicated, add a new user with those values and print `USER CREATED`.

#### Scenario: Valid non-duplicate user created
- **GIVEN** no registered users
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is printed
- **AND** a user with DNI `12345678Z` and email `ana@correo.com` is registered with all given values

### Requirement: Validation before storing
User data SHALL be validated with the user validation rules (DNI, email, password, card number) before the user is stored; if any value is invalid, the user MUST NOT be added and an error message starting with `ERROR` naming the invalid field SHALL be printed.

#### Scenario: Invalid DNI rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an error message starting with `ERROR` is printed and the number of registered users is unchanged

#### Scenario: Invalid email, password or card rejected
- **WHEN** a `USER CREATE` command with email `ana@correo.es`, or password `Clave12`, or card `1234 5678 9012 3456` (other data valid) is processed
- **THEN** an error message starting with `ERROR` is printed and no user is added

### Requirement: DNI and email uniqueness
Before creating a user the system SHALL check that no registered user has the same DNI and that no registered user has the same email address; if either exists, creation MUST be rejected with an error message starting with `ERROR` and the registered users left unchanged.

#### Scenario: Duplicate DNI rejected
- **GIVEN** a registered user with DNI `12345678Z`
- **WHEN** `USER CREATE;Luis;Martin;12345678Z;luis@correo.com;Clave1!;600000000;1111222233334444` is processed
- **THEN** an error message starting with `ERROR` is printed and only the original user with that DNI exists

#### Scenario: Duplicate email rejected
- **GIVEN** a registered user with email `ana@correo.com`
- **WHEN** `USER CREATE;Luis;Martin;87654321X;ana@correo.com;Clave1!;600000000;1111222233334444` is processed
- **THEN** an error message starting with `ERROR` is printed and no user with DNI `87654321X` exists
