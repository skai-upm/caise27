# Spec Delta

## ADDED Requirements

### Requirement: User registration
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL add a new user when the data passes the user data validation. The password SHALL be protected before it is stored, and a confirmation SHALL be shown. A malformed command (wrong number of fields) or invalid data SHALL be rejected with an error, no user SHALL be added, and command processing SHALL continue.

#### Scenario: Valid user created
- **GIVEN** no user with DNI `12345678Z` or e-mail `ana@correo.com`
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is shown, and a user with those data exists whose stored password is a BCrypt value that verifies `Clave1!`

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is processed with DNI `12345678A`, e-mail `ana@correo.es`, password `clave1!` or card `123`
- **THEN** an error naming the violated rule is shown and no user is added

#### Scenario: Malformed command
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed, followed by `HELP`
- **THEN** a syntax error is shown, no user is added, and `HELP` is still executed

### Requirement: Unique DNI and e-mail
Before a user is created, the system SHALL check that no existing user has the same DNI and that no existing user has the same e-mail address. A duplicate SHALL be rejected with an error and no user SHALL be added.

#### Scenario: Duplicate DNI
- **GIVEN** a user with DNI `12345678Z`
- **WHEN** a user with DNI `12345678Z` and a different e-mail is created
- **THEN** an error is shown and only the original user exists

#### Scenario: Duplicate e-mail
- **GIVEN** a user with e-mail `ana@correo.com`
- **WHEN** a user with e-mail `ana@correo.com` and a different DNI is created
- **THEN** an error is shown and only the original user exists

### Requirement: Registered users usable by park operations
Registered users SHALL be the users that park operations recognise: group membership, ticket purchase and group-ticket identifiers (derived from the DNI) SHALL resolve users through the registry.

#### Scenario: Registered user buys a ticket
- **GIVEN** `ana@correo.com` registered with `USER CREATE`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;<today>` is processed
- **THEN** the ticket is created for that user
