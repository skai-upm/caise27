# user-management Specification

## Purpose

Lets park users be registered, authenticated for changes, updated, and deleted through text commands, with strict validation of identity and payment data and uniqueness of DNI and email.

## Requirements

### Requirement: User data model
Each user SHALL hold first name, surname(s), DNI, email address, password, telephone number, and card number. The DNI SHALL be immutable after the user is created.

#### Scenario: Immutable DNI
- **GIVEN** an existing user
- **WHEN** any `USER UPDATE` is requested
- **THEN** the user's DNI is not changed

### Requirement: Semicolon-delimited user commands
User commands SHALL be text lines whose parts are separated by `;`. The first part names the operation (`USER CREATE`, `USER DELETE`, or `USER UPDATE`) and the remaining parts are its parameters in the documented order. Leading and trailing whitespace around each part SHALL be ignored. A user command with a missing, empty, or extra parameter SHALL be rejected with an error message and SHALL NOT change any user.

#### Scenario: Insufficient parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error message is reported and no user is created

#### Scenario: Empty parameter
- **WHEN** `USER CREATE;;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an error message is reported and no user is created

### Requirement: User creation
The system SHALL accept `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`. It SHALL validate all parameters and uniqueness before storing the user, and SHALL reject an invalid request without storing any part of the user. The telephone number SHALL be stored exactly as given.

#### Scenario: Valid registration
- **GIVEN** there is no registered user with DNI `12345678Z` or email `ana@correo.com`
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is created, stored, and a success message is reported

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one uppercase letter, and the letter SHALL equal the letter given by the standard Spanish DNI algorithm (the number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`). Format-only checks SHALL NOT suffice.

#### Scenario: Valid DNI
- **WHEN** a creation request uses DNI `12345678Z`
- **THEN** the DNI is accepted

#### Scenario: Wrong control letter
- **WHEN** an otherwise valid creation request uses DNI `12345678A`
- **THEN** creation is rejected

#### Scenario: Malformed DNI
- **WHEN** an otherwise valid creation request uses a DNI with fewer than eight digits, non-digit characters in the first eight positions, or no final letter
- **THEN** creation is rejected

### Requirement: Email validation
An email SHALL follow the shape `texto@texto.xxx`: exactly one `@`, a nonempty part before `@`, a part after `@` that contains a period, a nonempty domain before the last period, and an extension after the last period of exactly three characters. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** a creation request uses email `ana@correo.com`
- **THEN** the email is accepted

#### Scenario: Invalid emails
- **WHEN** an otherwise valid creation request uses any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, or `ana@correo.es`
- **THEN** creation is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit, and one symbol (a character that is neither a letter, a digit, nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** a creation request uses password `Clave1!`
- **THEN** the password is accepted

#### Scenario: Password missing a category
- **WHEN** an otherwise valid creation request uses any of `clave1!`, `CLAVE1!`, `Clave!!`, or `Clave12`
- **THEN** creation is rejected without storing the user

### Requirement: Card number validation
A card number SHALL consist of exactly 16 characters, all of them digits; spaces, letters, and symbols SHALL NOT be accepted.

#### Scenario: Valid card
- **WHEN** a creation request uses card `1234567890123456`
- **THEN** the card is accepted

#### Scenario: Invalid cards
- **WHEN** an otherwise valid creation request uses any of `1234 5678 9012 3456`, `123456789012345`, or `12345678901234567`
- **THEN** creation is rejected without storing the user

### Requirement: DNI and email uniqueness
No two registered users SHALL share a DNI or an email address. Both constraints SHALL be checked before a user is created and the email constraint SHALL be checked before an email is updated.

#### Scenario: Duplicate DNI
- **GIVEN** an existing user with DNI `12345678Z` and email `ana@correo.com`
- **WHEN** a user is created with DNI `12345678Z` and email `otro@correo.com`
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **GIVEN** an existing user with DNI `12345678Z` and email `ana@correo.com`
- **WHEN** a user is created with DNI `87654321X` and email `ana@correo.com`
- **THEN** creation is rejected

### Requirement: Authenticated user deletion
The system SHALL accept `USER DELETE;Correo;Contraseña`. It SHALL find the user by email, check that the supplied password equals the stored password, and check that the user holds no individual or group-constituent ticket dated on or after the current system date; only then SHALL it delete the user, remove them from any group and attraction, and update persistent storage. Past tickets of the user SHALL be kept as history.

#### Scenario: Authenticated deletion
- **GIVEN** an existing user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted, persistence is updated, and a success message is reported

#### Scenario: Deletion with wrong credentials
- **GIVEN** an existing user
- **WHEN** `USER DELETE` receives an incorrect password or a nonexistent email
- **THEN** an error message is reported and no user is deleted

#### Scenario: Current or future ticket blocks user deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and a ticket dated `2026-10-15` for Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected and the user and ticket remain intact

#### Scenario: Ticket dated today blocks user deletion
- **GIVEN** `SYSTEM DATE;2026-10-15` and a ticket dated `2026-10-15` for Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected

#### Scenario: Historical ticket no longer blocks deletion
- **GIVEN** a ticket dated `2026-10-15` for Ana and system date changed to `2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** Ana is deleted, removed from her groups, and the ticket remains as a historical record

#### Scenario: Group-ticket constituent blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and a group ticket dated `2026-10-15` that includes Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** deletion is rejected

### Requirement: Authenticated user update
The system SHALL accept `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, or `CARD`. It SHALL check the current email and password before any modification, validate the new value with the same rules used at creation, and persist the change on success.

#### Scenario: Successful field update
- **GIVEN** an existing user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the user's first name becomes `Ana Maria`, the change is persisted, and a success message is reported

#### Scenario: Chained updates using new credentials
- **GIVEN** an existing user with email `ana@correo.com` and password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** all three updates succeed and the user has the new email, password, and card

#### Scenario: Update with wrong credentials
- **GIVEN** an existing user
- **WHEN** `USER UPDATE` receives an incorrect current password or a nonexistent email
- **THEN** an error message is reported and the user is unchanged

#### Scenario: Email update uniqueness
- **GIVEN** two different registered users
- **WHEN** one user attempts to update their email to that of the other
- **THEN** the update is rejected

#### Scenario: Password or card revalidation
- **GIVEN** an existing user with valid current credentials
- **WHEN** an update supplies a password failing the character rules, a card failing the 16-digit rule, or an email failing the email format
- **THEN** the update is rejected and the original state remains intact

#### Scenario: DNI is not an updatable field
- **GIVEN** an existing user with valid current credentials
- **WHEN** `USER UPDATE` names the field `DNI` or any other field outside the allowed set
- **THEN** an error message is reported and the user is unchanged
