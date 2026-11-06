# users Specification

## Purpose
Defines the user as an independent domain object of the amusement park: the data each user holds, how that data is encapsulated, and which parts of a user's identity can never change.

## Requirements

### Requirement: User attributes
Each user SHALL hold a first name, surname(s), DNI, e-mail address, password (its stored protected value), telephone number and card number. The attributes SHALL be encapsulated: other objects SHALL read them through the user's operations and SHALL NOT access its state directly. A user SHALL be created with all its attributes present.

#### Scenario: User created with its data
- **GIVEN** the data `Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, a protected password, `612345678` and `1234567890123456`
- **WHEN** a user is created with it
- **THEN** each attribute can be read back with the same value

#### Scenario: Missing attribute rejected
- **WHEN** a user is created without one of its attributes
- **THEN** creation is rejected and no user exists

### Requirement: Immutable DNI
A user's DNI SHALL NOT change after creation. The other attributes MAY be changed through the user's own operations, for later management features.

#### Scenario: DNI cannot be changed
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** an attempt is made to change the DNI
- **THEN** no operation allows it and the DNI remains `12345678Z`

#### Scenario: Other data can change
- **GIVEN** an existing user
- **WHEN** their phone is changed to `699999999`
- **THEN** the user's phone is `699999999` and the DNI is unchanged

### Requirement: Users do not manage persistence
The user class SHALL represent the user only. It SHALL NOT read or write files, and storing and retrieving users SHALL be the responsibility of a separate class. The application entry point SHALL NOT contain user file I/O.

#### Scenario: Separation checked
- **WHEN** the user class is inspected
- **THEN** it has no file or persistence dependency

### Requirement: DNI validation
A user's DNI SHALL consist of exactly eight digits followed by one uppercase letter. The letter SHALL be the one assigned to the number by the standard Spanish algorithm: the number modulo 23 indexes the sequence `TRWAGMYFPDXBNJZSQVHLCKE`. Any other DNI SHALL be rejected.

#### Scenario: Valid DNI
- **WHEN** `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter or format
- **WHEN** `12345678A`, `1234567Z`, `123456789Z` or `1234567AZ` is validated
- **THEN** it is rejected

### Requirement: E-mail validation
A user's e-mail SHALL have the form `text@text.xxx`, checked with String operations and `split`, not regular expressions. It SHALL contain exactly one `@`, with content before it, a non-empty domain after it, and an extension of exactly three characters after the last dot, and no whitespace.

#### Scenario: Valid e-mail
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid e-mails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A user's password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (any character that is not a letter, digit or whitespace). It SHALL be checked without regular expressions or external libraries, and before the password is protected for storage.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing category
- **WHEN** `clave1!` (no uppercase), `CLAVE1!` (no lowercase), `Clave!!` (no digit) or `Clave12` (no symbol) is validated
- **THEN** each is rejected

### Requirement: Card number validation
A user's card number SHALL be exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** each is rejected

### Requirement: Reusable user data validation
The user data rules SHALL be available as one reusable validation of DNI, e-mail, password and card number. It SHALL reject invalid data with an error naming the first violated rule, so that every user operation applies the same checks. The telephone number SHALL only be stored, without validation.

#### Scenario: Combined validation
- **WHEN** data with a valid DNI, e-mail and password but card `123` is validated
- **THEN** it is rejected with an error about the card number

#### Scenario: Phone not validated
- **WHEN** valid data with phone `abc` is validated
- **THEN** it is accepted

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

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by e-mail, verify that the user exists, check the entered password against the stored protected value, delete the user (removing them from all groups) and update persistent data. An unknown e-mail, a wrong password, or the ticket and group-lock deletion rules SHALL reject the deletion with an error, leaving the user and all related data unchanged. Historical tickets of a deleted user SHALL be kept.

#### Scenario: Correct credentials
- **GIVEN** registered user `ana@correo.com` with password `Clave1!` and no active tickets
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is shown, the user no longer exists, and after a restart the user is still absent

#### Scenario: Wrong password
- **GIVEN** registered user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error is shown and the user still exists unchanged

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error is shown

#### Scenario: Active ticket blocks deletion
- **GIVEN** `ana@correo.com` has a ticket dated today
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is shown and the user still exists

#### Scenario: Removed from groups, history kept
- **GIVEN** `ana@correo.com` belongs to an unlocked group and has only past tickets
- **WHEN** the user is deleted with correct credentials
- **THEN** the user is no longer a member of the group and the past tickets still exist
