# user-registration Specification

## Purpose
Defines how new users are registered with the `USER CREATE` command: the command format, validation of the data before storing, uniqueness of DNI and email, and the reported outcome.

## Requirements

### Requirement: USER CREATE command format
The system SHALL accept the command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` with exactly seven data elements after the command name. A `USER CREATE` command with a different number of elements MUST be rejected with an `ERROR` response and no user added.

#### Scenario: Wrong number of elements
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678` (six data elements) is processed
- **THEN** an `ERROR` response is printed
- **AND** no user is stored

### Requirement: Successful user creation
When all data elements are valid and neither the DNI nor the email belongs to an existing user, `USER CREATE` SHALL store a new user with exactly the given first name, surnames, DNI, email, password, telephone and card number, and print a response starting with `OK`.

#### Scenario: Valid non-duplicate user created
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** a response starting with `OK` is printed
- **AND** a user with DNI `12345678Z` and all the given attributes is stored

### Requirement: Data validated before storing
User data SHALL be validated with the user-validation rules (DNI, email, password, card number) before the user is stored; if any element is invalid the command MUST be rejected with an `ERROR` response that names the invalid field, and no user is stored.

#### Scenario: Invalid DNI rejected
- **WHEN** `USER CREATE` is processed with DNI `12345678A` and otherwise valid data
- **THEN** an `ERROR` response mentioning the DNI is printed and no user is stored

#### Scenario: Invalid email rejected
- **WHEN** `USER CREATE` is processed with email `ana@correo.es` and otherwise valid data
- **THEN** an `ERROR` response mentioning the email is printed and no user is stored

#### Scenario: Invalid password rejected
- **WHEN** `USER CREATE` is processed with password `Clave12` and otherwise valid data
- **THEN** an `ERROR` response mentioning the password is printed and no user is stored

#### Scenario: Invalid card rejected
- **WHEN** `USER CREATE` is processed with card `123456789012345` and otherwise valid data
- **THEN** an `ERROR` response mentioning the card is printed and no user is stored

### Requirement: DNI and email uniqueness
Two users MUST NOT share the same DNI or the same email address. Both conditions SHALL be checked before a user is created; a `USER CREATE` whose DNI or email already belongs to a stored user MUST be rejected with an `ERROR` response, leaving the stored users unchanged.

#### Scenario: Duplicate DNI rejected
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and a different email
- **THEN** an `ERROR` response mentioning the DNI is printed
- **AND** the number of stored users and the existing user's data are unchanged

#### Scenario: Duplicate email rejected
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with a different valid DNI and email `ana@correo.com`
- **THEN** an `ERROR` response mentioning the email is printed
- **AND** the number of stored users is unchanged
