# Spec Delta

## ADDED Requirements

### Requirement: User creation command
The application SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and, when all checks pass, store a new user with that data and confirm it with `USER CREATED`. The command SHALL have exactly seven data elements after the command name.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is printed and a user with email `ana@correo.com` and DNI `12345678Z` exists

#### Scenario: Wrong number of elements
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error describing the expected syntax is printed and no user is created

### Requirement: Validation before storing
User data SHALL be validated with the DNI, email, password and card rules before a user is stored; if any rule fails, no user SHALL be created and the failing fields SHALL be reported.

#### Scenario: Invalid DNI rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an error about the DNI is printed and no user is created

#### Scenario: Invalid password rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;clave1!;612345678;1234567890123456` is processed
- **THEN** an error about the password is printed and no user is created

### Requirement: Unique DNI and email
Two users SHALL NOT have the same DNI or the same email address; both conditions SHALL be checked before creating a user.

#### Scenario: Duplicate DNI rejected
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otra@correo.com`
- **THEN** an error about the duplicate DNI is printed and no user is created

#### Scenario: Duplicate email rejected
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** an error about the duplicate email is printed and no user is created
