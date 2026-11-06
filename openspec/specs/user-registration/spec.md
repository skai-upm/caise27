# user-registration Specification

## Purpose

Lets new users be added to the Amusement Park with the `USER CREATE` command, guaranteeing that only valid users with a unique DNI and email address are stored.

## Requirements

### Requirement: Create user command
The system SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and, when the data is valid and unique, store a new user with exactly those values and print a confirmation that names the user's DNI.

#### Scenario: Valid non-duplicate user is created
- **GIVEN** no user with DNI `12345678Z` or email `ana@correo.com` exists
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** a user with exactly those seven values is stored and a confirmation message containing `12345678Z` is printed

### Requirement: Data validated before storing
User data SHALL be validated with the `valid-users` rules (DNI, email, password, card number) before the user is stored; when any field is invalid the command MUST be rejected with an error message naming the first invalid field (checked in the order DNI, email, password, card number) and no user is added.

#### Scenario: Invalid DNI is rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an error naming the DNI is printed and the number of stored users is unchanged

#### Scenario: Invalid email, password or card is rejected
- **WHEN** a `USER CREATE` command is processed whose only invalid field is the email `ana@correo.es`, or the password `clave1!`, or the card `1234 5678 9012 3456`
- **THEN** an error naming that field is printed and no user is added

### Requirement: Unique DNI and email
Two users SHALL NOT share the same DNI nor the same email address. Both conditions MUST be checked before creating a user; a command that would duplicate either is rejected with an error message and leaves the stored users unchanged.

#### Scenario: Duplicate DNI is rejected
- **GIVEN** a stored user with DNI `12345678Z`
- **WHEN** `USER CREATE;Luis;Perez;12345678Z;luis@correo.com;Clave1!;600000000;1111222233334444` is processed
- **THEN** an error reporting the duplicate DNI is printed and only the original user is stored, unchanged

#### Scenario: Duplicate email is rejected
- **GIVEN** a stored user with email `ana@correo.com`
- **WHEN** `USER CREATE;Luis;Perez;00000000T;ana@correo.com;Clave1!;600000000;1111222233334444` is processed
- **THEN** an error reporting the duplicate email is printed and no user with DNI `00000000T` is stored
