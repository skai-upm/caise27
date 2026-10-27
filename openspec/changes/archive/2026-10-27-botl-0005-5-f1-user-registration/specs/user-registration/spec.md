# Spec Delta

## Purpose

Defines the `USER CREATE` command that registers new users after validating their data and guaranteeing unique DNI and email address.

## ADDED Requirements

### Requirement: USER CREATE syntax
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL register a user when it carries exactly seven non-empty fields; otherwise it SHALL be rejected with a format error and no user is created.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** the response is `USER CREATED` and a user with DNI `12345678Z` and email `ana@correo.com` exists

#### Scenario: Missing fields
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error is reported and no user is created

### Requirement: Data validated before storing
User data SHALL be validated with the DNI, email, password and card rules before the user is stored; any invalid value rejects the command.

#### Scenario: Invalid DNI rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an error about the DNI is reported and no user is created

#### Scenario: Invalid email rejected
- **WHEN** a USER CREATE command has email `ana@correo.es`
- **THEN** an error about the email is reported and no user is created

#### Scenario: Invalid password rejected
- **WHEN** a USER CREATE command has password `Clave12`
- **THEN** an error about the password is reported and no user is created

#### Scenario: Invalid card rejected
- **WHEN** a USER CREATE command has card `1234 5678 9012 3456`
- **THEN** an error about the card number is reported and no user is created

### Requirement: Unique DNI and email
A user SHALL NOT be created when another user already has the same DNI or the same email address; both conditions are checked before creating the user.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and USER CREATE is processed with DNI `12345678Z` and a new email
- **THEN** an error about the duplicate DNI is reported and the number of users is unchanged

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and USER CREATE is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** an error about the duplicate email is reported and the number of users is unchanged
