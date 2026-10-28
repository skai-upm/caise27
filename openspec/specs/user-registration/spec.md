# user-registration Specification

## Purpose
Defines user registration through the `USER CREATE` command: its arguments, the validation applied before storing, and the uniqueness of DNI and email address among stored users.

## Requirements

### Requirement: USER CREATE command
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create and store a new user with exactly those seven values and print `USER CREATED`.

#### Scenario: Valid user is created
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no user with that DNI or email exists
- **THEN** `USER CREATED` is printed and a user with DNI `12345678Z` and those values is stored

### Requirement: Validation before storing
User data SHALL be validated with the user-validation rules (DNI, email, password, card number) before the user is stored; if any value is invalid the user SHALL NOT be stored and an `ERROR:` line naming the invalid field SHALL be printed.

#### Scenario: Invalid DNI
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an `ERROR:` line about the DNI is printed and no user is stored

#### Scenario: Invalid email, password or card
- **WHEN** a `USER CREATE` command has an email `ana@correo.es`, or a password `clave1!`, or a card `1234 5678 9012 3456`, the other data being valid
- **THEN** an `ERROR:` line about that field is printed and no user is stored

### Requirement: DNI and email uniqueness
Before creating a user the system SHALL check that no stored user has the same DNI and that no stored user has the same email address; if either exists the user SHALL NOT be created and an `ERROR:` line SHALL be printed.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and a different email
- **THEN** an `ERROR:` line about the duplicate DNI is printed and the stored users are unchanged

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with email `ana@correo.com` and a different valid DNI
- **THEN** an `ERROR:` line about the duplicate email is printed and the stored users are unchanged
