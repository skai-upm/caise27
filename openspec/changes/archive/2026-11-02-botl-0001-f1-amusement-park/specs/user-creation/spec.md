# Spec Delta

## Purpose

Defines the `USER CREATE` command that registers new users after validating their data and enforcing DNI and email uniqueness (F1-C03).

## ADDED Requirements

### Requirement: USER CREATE command
The system SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, with elements separated by `;` and interpreted using basic String operations including `split`. When all data is valid and unique, exactly one user SHALL be registered and the command SHALL report success (for example `USER CREATED`).

#### Scenario: Valid unique user is created
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** exactly one user with those attributes is registered and success is reported

### Requirement: Validation before creation
User data SHALL be validated with the user-data-validation rules before a user is stored. If any value is invalid, or the command does not have exactly the seven data fields, no user SHALL be registered and an error SHALL be reported.

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is processed with DNI `12345678A`
- **THEN** no user is registered and an error is reported

#### Scenario: Malformed command rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** no user is registered, an error is reported, and the application keeps processing commands

### Requirement: DNI and email uniqueness
Two users SHALL NOT share the same DNI or the same email address. Both conditions SHALL be checked before a user is created.

#### Scenario: Duplicate DNI rejected
- **WHEN** `USER CREATE` is processed with the DNI of an existing user and a new email
- **THEN** no additional user is registered and an error is reported

#### Scenario: Duplicate email rejected
- **WHEN** `USER CREATE` is processed with the email of an existing user and a new DNI
- **THEN** no additional user is registered and an error is reported

### Requirement: Unknown or incorrect commands are controlled
An unrecognised or incorrect command SHALL be reported as an error and SHALL NOT terminate the application unexpectedly.

#### Scenario: Unknown command
- **WHEN** `FOO BAR` is processed
- **THEN** an error is reported and the next command is still processed
