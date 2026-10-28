# user-management Specification

## Purpose
Defines the park user: its attributes, how they are encapsulated, and which of them may change, as the basis for user validation, registration, deletion and modification.

## Requirements

### Requirement: User attributes
Each user SHALL have a first name, surname(s), DNI, e-mail address, password, telephone number and card number, held as private attributes accessible only through the user's operations. The password SHALL be kept only in its protected (BCrypt) form.

#### Scenario: Complete user
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, e-mail `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456`
- **THEN** the user exposes those values (the password only as a protected value that verifies `Clave1!`)

#### Scenario: Encapsulation
- **WHEN** the user class is inspected
- **THEN** all its instance attributes are private

### Requirement: Immutable DNI
A user's DNI SHALL NOT be changeable after creation, while the other attributes MAY be modified through the user's operations.

#### Scenario: No way to change the DNI
- **WHEN** the operations of the user class are inspected
- **THEN** the DNI attribute is final and there is no operation that modifies it, whereas e-mail, names, telephone, card and protected password have modifiers

### Requirement: DNI validation
A user's DNI SHALL consist of eight digits followed by the upper-case letter that corresponds to the number under the standard Spanish DNI algorithm (letter at position number mod 23 of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Valid and invalid DNI
- **WHEN** the DNIs `12345678Z`, `12345678A`, `1234567Z`, `123456789` and `1234567AZ` are validated
- **THEN** only `12345678Z` is valid

### Requirement: E-mail validation
A user's e-mail SHALL have the form `text@text.xxx`: content before a single `@`, a domain after it, and an extension of exactly three characters after the last dot, checked with basic `String` operations and `split`, without regular expressions.

#### Scenario: Valid and invalid e-mails
- **WHEN** `ana@correo.com`, `ana`, `ana@correo`, `@correo.com`, `ana@.com` and `ana@correo.es` are validated
- **THEN** only `ana@correo.com` is valid

### Requirement: Password validation
A user's password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol, checked without regular expressions or external libraries.

#### Scenario: Valid and invalid passwords
- **WHEN** `Clave1!`, `clave1!`, `CLAVE1!`, `Clave!!` and `Clave12` are validated
- **THEN** only `Clave1!` is valid

### Requirement: Card number validation
A user's card number SHALL contain exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid and invalid cards
- **WHEN** `1234567890123456`, `1234 5678 9012 3456`, `123456789012345` and `12345678901234567` are validated
- **THEN** only `1234567890123456` is valid

### Requirement: Reusable user data validation
The validation SHALL be available to user operations as a single check of all user data that rejects invalid data with an error naming the invalid field; the telephone number SHALL only be stored, without validation.

#### Scenario: Whole user validation
- **WHEN** user data with a valid DNI, e-mail, password and card is validated, and then the same data with the card `1234`
- **THEN** the first passes and the second is rejected with an error about the card number

### Requirement: USER CREATE command
The system SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and, when all the data satisfies the user validation rules and no active user has the same DNI or e-mail, register the user with all its attributes (the password only in protected form) and display a confirmation. Otherwise, or with a wrong number of fields or empty fields, an error message SHALL be displayed, no user SHALL be created and processing SHALL continue.

#### Scenario: Create a user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is displayed and user `ana@correo.com` exists with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, telephone `612345678`, card `1234567890123456` and a protected password that verifies `Clave1!`

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is processed with DNI `12345678A`, or e-mail `ana@correo.es`, or password `clave1!`, or card `1234 5678 9012 3456`
- **THEN** an error message is displayed and no user is created

#### Scenario: Malformed command
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** an error message is displayed and the next command is still processed

### Requirement: User uniqueness
Two users SHALL NOT have the same DNI or the same e-mail address; both conditions SHALL be checked before creating a user.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and a new e-mail
- **THEN** an error message is displayed and no user is created

#### Scenario: Duplicate e-mail
- **WHEN** a user with e-mail `ana@correo.com` exists and `USER CREATE` is processed with that e-mail and a new valid DNI
- **THEN** an error message is displayed and no user is created

### Requirement: USER DELETE command
The system SHALL accept `USER DELETE;Email;Password` and, in this order, locate the user by e-mail, verify that it exists, check the password against the stored protected value, delete the user and update the persistent data. An unknown e-mail or an incorrect password SHALL be rejected with an error message without deleting anything; the ticket and group restrictions on user deletion SHALL still apply.

#### Scenario: Delete with the correct password
- **WHEN** user `ana@correo.com` with password `Clave1!` and no active tickets exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is displayed and the user no longer exists, also after restarting the application

#### Scenario: Incorrect password
- **WHEN** `USER DELETE;ana@correo.com;Clave1?` is processed
- **THEN** an error message is displayed and the user still exists

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error message is displayed

#### Scenario: Active ticket still blocks deletion
- **WHEN** `ana@correo.com` has a ticket dated today and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message is displayed and the user still exists
