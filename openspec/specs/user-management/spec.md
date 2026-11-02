# user-management Specification

## Purpose

Defines the user data held by the amusement-park application and the text commands that create, delete, and update users with validated, unique data.

## Requirements

### Requirement: User data
Each user SHALL hold a first name, surname(s), DNI, email address, password, telephone number, and card number. The DNI SHALL NOT change after the user has been created.

#### Scenario: DNI cannot be updated
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed for an existing user with valid credentials
- **THEN** the update is rejected and the user's DNI is unchanged

### Requirement: Semicolon-delimited user commands
User commands SHALL be semicolon-delimited text whose first field is the operation name (`USER CREATE`, `USER DELETE`, or `USER UPDATE`) followed by its parameters in the documented order. A command with an unknown operation or the wrong number of parameters SHALL be rejected with an error message and SHALL NOT change any user.

#### Scenario: Insufficient parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** an error message is shown and no user is created

#### Scenario: Unknown operation
- **WHEN** `USER LIST` is processed
- **THEN** an error message is shown and no user changes

### Requirement: Create user
The system SHALL accept `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, validate every field before storing, and create and persist the user only when all fields are valid. Name, surname(s) and telephone SHALL be non-empty; the telephone SHALL be stored exactly as given.

#### Scenario: Valid registration
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no user has DNI `12345678Z` or email `ana@correo.com`
- **THEN** the user is created, persisted, and a success message is shown

#### Scenario: Invalid data is not stored
- **WHEN** a `USER CREATE` command contains any invalid field
- **THEN** an error message is shown and no user is stored

#### Scenario: Empty required field
- **WHEN** a `USER CREATE` command has an empty first name, surname or telephone
- **THEN** creation is rejected

### Requirement: Spanish DNI validation
A DNI SHALL consist of exactly eight digits followed by one letter, and the letter SHALL equal the letter given by the standard Spanish DNI algorithm (the eight-digit number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`). Format-only checks SHALL NOT suffice.

#### Scenario: Correct check letter
- **WHEN** the DNI is `12345678Z`
- **THEN** the DNI is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI is `12345678A`
- **THEN** creation is rejected

#### Scenario: Wrong shape
- **WHEN** the DNI is `1234567Z` or `123456789Z` or `12345678`
- **THEN** creation is rejected

### Requirement: Email validation
An email SHALL follow `texto@texto.xxx`: exactly one `@`, a non-empty part before it, a part after it containing a period, a non-empty domain name before the last period, and an extension after the last period of exactly three characters. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** the email is `ana@correo.com`
- **THEN** the email is accepted

#### Scenario: Invalid emails
- **WHEN** the email is `ana`, `ana@correo`, `@correo.com`, `ana@.com`, or `ana@correo.es`
- **THEN** creation is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit, and one symbol (a character that is neither a letter, a digit, nor whitespace). Validation SHALL NOT use regular expressions or third-party libraries.

#### Scenario: Valid password
- **WHEN** the password is `Clave1!`
- **THEN** the password is accepted

#### Scenario: Missing character category
- **WHEN** the password is `clave1!`, `CLAVE1!`, `Clave!!`, or `Clave12`
- **THEN** creation is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits with no spaces, letters, or symbols.

#### Scenario: Valid card
- **WHEN** the card number is `1234567890123456`
- **THEN** the card number is accepted

#### Scenario: Invalid cards
- **WHEN** the card number is `1234 5678 9012 3456`, `123456789012345`, or `12345678901234567`
- **THEN** creation is rejected

### Requirement: User uniqueness
No two registered users SHALL share a DNI or an email address; both constraints SHALL be checked before a user is created.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` and email `ana@correo.com` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **WHEN** a user with DNI `12345678Z` and email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** creation is rejected

### Requirement: Delete user
The system SHALL accept `USER DELETE;Correo;Contraseña`, find the user by email, and delete the user and update persistent storage only when the user exists and the password matches the stored password.

#### Scenario: Authenticated deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** the user is deleted and persistent storage no longer contains it

#### Scenario: Wrong credentials on delete
- **WHEN** `USER DELETE` is processed with a nonexistent email or an incorrect password
- **THEN** an error message is shown and no user is deleted

### Requirement: Update user
The system SHALL accept `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, verify the current email and password before any change, and update exactly one field chosen from `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, or `CARD`. A new value SHALL satisfy the same validation and uniqueness rules as at creation, and a successful update SHALL be persisted.

#### Scenario: Update first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with valid credentials
- **THEN** the user's first name becomes `Ana Maria` and the change is persisted

#### Scenario: Update email then authenticate with new email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed and then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`
- **THEN** both updates succeed and the user is identified by `ana.nueva@correo.com` with password `Nueva2#`

#### Scenario: Wrong credentials on update
- **WHEN** `USER UPDATE` is processed with a nonexistent email or an incorrect current password
- **THEN** an error message is shown and no user changes

#### Scenario: Email taken by another user
- **WHEN** a user attempts to update their email to the email of another registered user
- **THEN** the update is rejected and both users are unchanged

#### Scenario: Invalid new password or card
- **WHEN** an update supplies a password missing a required character category or a card that is not exactly 16 digits
- **THEN** the update is rejected and the user's original state is unchanged

#### Scenario: Unknown field
- **WHEN** `USER UPDATE` names a field outside the updatable set
- **THEN** the update is rejected and the user is unchanged
