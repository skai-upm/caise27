# Spec Delta

## ADDED Requirements

### Requirement: USER CREATE command
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create and register a user after validating its data with the user data rules; the password SHALL be stored only in protected form. Invalid data or a wrong number of fields SHALL be rejected with an error message, nothing SHALL be created, and processing SHALL continue.

#### Scenario: Source example
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is shown and user `ana@correo.com` exists with all its data and a protected password that verifies `Clave1!`

#### Scenario: Invalid data rejected
- **WHEN** a USER CREATE line has DNI `12345678A` or password `clave1!` or card `1234 5678 9012 3456` or email `ana@correo.es`
- **THEN** an error message is shown and no user is created

#### Scenario: Wrong number of fields
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error message is shown and processing continues

### Requirement: Unique DNI and email
Before creating a user, the system SHALL check that no registered user has the same DNI and that no registered user has the same email address; otherwise the creation SHALL be rejected with an error message.

#### Scenario: Duplicate DNI
- **WHEN** `12345678Z` is registered and another USER CREATE uses DNI `12345678Z` with a different email
- **THEN** an error message is shown and no user is created

#### Scenario: Duplicate email
- **WHEN** `ana@correo.com` is registered and another USER CREATE uses that email with DNI `87654321X`
- **THEN** an error message is shown and no user is created
