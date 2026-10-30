# user-management Specification

## Purpose

Manages amusement-park users: their data model, validation of personal data, uniqueness rules, and the text commands to create, delete and update users.

## Requirements

### Requirement: User data model
Each user SHALL hold first name, surname(s), DNI, email address, password, telephone number and card number. User attributes SHALL be encapsulated and the DNI SHALL be immutable after creation. The user representation SHALL NOT read or write persistent storage itself.

#### Scenario: DNI cannot be changed
- **GIVEN** an existing user
- **WHEN** any update is requested through `USER UPDATE`
- **THEN** the user's DNI remains unchanged

#### Scenario: Responsibility separation
- **WHEN** the implementation is inspected
- **THEN** user representation, application start-up/terminal interaction, and user storage/loading are implemented in distinct classes

### Requirement: Command parsing
User commands SHALL be semicolon-delimited text interpreted with `String.split` and basic `String` operations, without external parsing libraries or regular expressions. Commands with an unknown operation or an insufficient number of parameters SHALL be rejected with an error message and SHALL NOT terminate the application.

#### Scenario: Insufficient parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** an error message is reported, no user is created, and the application keeps running

### Requirement: Create user
The system SHALL accept `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`. It SHALL validate all data and uniqueness before storing the user, and SHALL NOT store anything when any check fails.

#### Scenario: Valid registration
- **GIVEN** no registered user has DNI `12345678Z` or email `ana@correo.com`
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is created and persisted

#### Scenario: Invalid data is not stored
- **WHEN** a `USER CREATE` command contains any invalid field
- **THEN** creation is rejected with an error message and no user is persisted

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one letter, and the letter SHALL equal the letter given by the standard Spanish DNI algorithm (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`). Format-only checks SHALL NOT suffice.

#### Scenario: Correct checksum letter
- **WHEN** the DNI is `12345678Z`
- **THEN** it is accepted

#### Scenario: Wrong checksum letter
- **WHEN** the DNI is `12345678A`
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL match `texto@texto.xxx` checked with basic `String` operations and without regular expressions: a non-empty part before `@`, a non-empty part after `@` containing a period, a non-empty domain before the last period, and an extension of exactly three characters.

#### Scenario: Valid email
- **WHEN** the email is `ana@correo.com`
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** the email is `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es`
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace), checked with standard Java functionality and without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** the password is `Clave1!`
- **THEN** it is accepted

#### Scenario: Missing character category
- **WHEN** the password is `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12`
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits with no spaces, letters or symbols. The telephone number SHALL be stored as given, without transformation or validation.

#### Scenario: Valid card
- **WHEN** the card is `1234567890123456`
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** the card is `1234 5678 9012 3456`, `123456789012345` or `12345678901234567`
- **THEN** it is rejected

### Requirement: User uniqueness
No two registered users SHALL share a DNI or an email address. Both constraints SHALL be checked before creating a user, and the email constraint SHALL be checked before changing a user's email.

#### Scenario: Duplicate DNI
- **GIVEN** a user with DNI `12345678Z` and email `ana@correo.com` exists
- **WHEN** a user is created with DNI `12345678Z` and email `otro@correo.com`
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **GIVEN** a user with DNI `12345678Z` and email `ana@correo.com` exists
- **WHEN** a user is created with DNI `87654321X` and email `ana@correo.com`
- **THEN** creation is rejected

### Requirement: Delete user
The system SHALL accept `USER DELETE;Correo;Contraseña`. It SHALL locate the user by email, verify the password matches, delete the user and update persistent storage. If the email does not exist or the password is wrong, nothing SHALL be deleted. Deletion SHALL also be rejected while the user has any ticket (individual or within a group ticket) dated on or after the system date. A deleted user SHALL be removed from every group; past tickets SHALL be kept as history.

#### Scenario: Authenticated deletion
- **GIVEN** a user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and the persistent file no longer contains it

#### Scenario: Wrong credentials on delete
- **WHEN** `USER DELETE` receives a nonexistent email or an incorrect password
- **THEN** no user is deleted and an error message is reported

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and ticket `T001` for Ana dated 2026-10-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected and Ana and `T001` remain

#### Scenario: Historical ticket does not block deletion
- **GIVEN** the ticket above and `SYSTEM DATE;2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** Ana is deleted, removed from her groups, and `T001` remains as a historical record

#### Scenario: Group ticket member cannot be deleted
- **GIVEN** Ana belongs to a group ticket dated on or after the system date
- **WHEN** Ana's deletion is requested
- **THEN** deletion is rejected

### Requirement: Update user
The system SHALL accept `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (represented by an enum). It SHALL verify current email and password before modifying anything, revalidate the new value with the creation rules, and persist successful changes. `DNI` SHALL NOT be updatable.

#### Scenario: Update first name
- **GIVEN** a user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the user's first name becomes `Ana Maria` and is persisted

#### Scenario: Wrong credentials on update
- **WHEN** `USER UPDATE` receives a nonexistent email or an incorrect password
- **THEN** no change is made

#### Scenario: Email already used by another user
- **GIVEN** two registered users
- **WHEN** one tries to change their email to the other's email
- **THEN** the update is rejected

#### Scenario: Invalid new password or card
- **WHEN** an update supplies a password failing the character rules or a card failing the 16-digit rule
- **THEN** the update is rejected and the user's state is unchanged

#### Scenario: Unknown or non-editable field
- **WHEN** the field is `DNI` or any value outside the editable set
- **THEN** the update is rejected and the user's state is unchanged
