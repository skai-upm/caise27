# users Specification

## Purpose
Defines the user of the Amusement Park application as an independent, encapsulated domain object with an immutable DNI, and the separate component that stores and retrieves users.

## Requirements

### Requirement: User attributes
A user SHALL have a first name, surname(s), DNI, email address, password, telephone number and card number. These attributes SHALL be accessible only through the user's operations, not as public fields.

#### Scenario: Create a user
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, a password, phone `612345678` and card `1234567890123456`
- **THEN** each value can be read back from the user

### Requirement: DNI is immutable
A user's DNI SHALL be fixed at creation and SHALL NOT be changeable afterwards. The other attributes MAY be changed through the user's operations.

#### Scenario: Attempt to change the DNI
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** the user's other attributes are changed
- **THEN** the DNI is still `12345678Z`, and the user offers no operation that changes it

### Requirement: Passwords are not kept in plain text by the user
The password attribute of a user SHALL hold the stored, protected value of the password, not the original text.

#### Scenario: User created with a protected password
- **GIVEN** the password `Clave1!` protected with BCrypt
- **WHEN** a user is created with that protected value
- **THEN** the user's password attribute does not contain `Clave1!`

### Requirement: Separate store for users
A component separate from the user class and from the application's main class SHALL store and retrieve users by email and by DNI. The user class SHALL NOT manage persistence. Central park management SHALL determine user existence and DNI through this component.

#### Scenario: Retrieve stored users
- **GIVEN** user `ana@correo.com` with DNI `12345678Z` is stored
- **WHEN** users are looked up by email `ana@correo.com` and by DNI `12345678Z`
- **THEN** the same user is returned both times

#### Scenario: Park management sees stored users
- **GIVEN** user `ana@correo.com` is stored and the application is initialized
- **WHEN** `TICKET CREATE;T001;ana@correo.com;<today>` is processed
- **THEN** the ticket is created for that user

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one uppercase letter equal to the letter at position (number mod 23) of `TRWAGMYFPDXBNJZSQVHLCKE`.

#### Scenario: Correct and incorrect check letters
- **WHEN** `12345678Z` and `12345678A` are validated
- **THEN** `12345678Z` is accepted and `12345678A` is rejected

#### Scenario: Wrong shape
- **WHEN** `1234567Z`, `123456789Z`, `1234567AZ` or `12345678` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by a final `.` and an extension of exactly three characters, with no spaces. The check SHALL use String operations, not regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). The check SHALL use neither regular expressions nor external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing categories
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 digits, with no spaces, letters or symbols.

#### Scenario: Card numbers
- **WHEN** `1234567890123456`, `1234 5678 9012 3456`, `123456789012345` and `12345678901234567` are validated
- **THEN** only `1234567890123456` is accepted

### Requirement: Whole-user validation for user operations
The system SHALL provide one validation of all the data of a user (first name, surnames, DNI, email, password, phone and card) that later user operations reuse. It SHALL reject the data with an invalid-input error naming the first invalid field. First name, surnames and phone SHALL only be required to be non-empty, and the phone SHALL be stored as given.

#### Scenario: Invalid field is named
- **WHEN** user data with DNI `12345678A` and otherwise valid values is validated
- **THEN** it is rejected with an error that mentions the DNI

### Requirement: Create a user
The system SHALL, for `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, validate all the data with the user data rules, check that no existing user has the same DNI or the same email, store the user with the password protected (never in plain text), and display `USER CREATED`. If any check fails or the command does not have exactly seven fields, it SHALL display an error message, add no user, and continue processing commands.

#### Scenario: Valid new user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is displayed
- **AND** user `ana@correo.com` with DNI `12345678Z` exists and its stored password is not `Clave1!` but verifies against it

#### Scenario: Duplicate DNI or email
- **GIVEN** user `ana@correo.com` with DNI `12345678Z` exists
- **WHEN** a USER CREATE uses DNI `12345678Z` with another email, or email `ana@correo.com` with another DNI
- **THEN** an error message is displayed and no user is added

#### Scenario: Malformed data
- **WHEN** USER CREATE is processed with an invalid DNI, email, password or card, or with a missing field
- **THEN** an error message is displayed and no user is added

### Requirement: DNI and email are unique
No two users SHALL share a DNI or an email address. Both conditions SHALL be checked before a user is stored.

#### Scenario: Both checked before storing
- **GIVEN** user `ana@correo.com` (DNI `12345678Z`) and user `pedro@correo.com` (DNI `87654321X`) exist
- **WHEN** a user with DNI `87654321X` and email `ana@correo.com` is created
- **THEN** it is rejected and the two existing users are unchanged
