# user-management Specification

## Purpose

Defines the park user model and the create, delete and update operations, with the validation and uniqueness rules each operation enforces.

## Requirements

### Requirement: User data
Each user SHALL have a first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created. All fields SHALL be non-empty.

#### Scenario: All fields stored
- **WHEN** a user is created with valid data
- **THEN** all seven fields are kept for that user exactly as supplied

### Requirement: Create user
The system SHALL create a user with `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` only when every field is valid and the DNI and email are not already used by another user. If any check fails, no user SHALL be stored and an error SHALL be reported.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is received and no user exists
- **THEN** the user is created and persisted, and a success message is printed

#### Scenario: Invalid field rejects creation
- **WHEN** a create command has a valid format but the password is `clave1!`
- **THEN** no user is created and an error naming the invalid field is printed

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one uppercase letter equal to the letter at index (number mod 23) of `TRWAGMYFPDXBNJZSQVHLCKE`.

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong format
- **WHEN** the DNI `1234567Z` or `123456789` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only if it has the form `texto@texto.xxx`: exactly one `@`, a non-empty part before it, a part after it containing a period, a non-empty domain name before the last period, and an extension after the last period of exactly three letters. Spaces SHALL NOT be allowed. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone number stored as given
The telephone number SHALL only be stored; it SHALL NOT be validated beyond being non-empty, transformed or encrypted.

#### Scenario: Phone stored
- **WHEN** a user is created with phone `612345678`
- **THEN** the stored phone is `612345678`

### Requirement: DNI and email uniqueness
The system SHALL NOT allow two users with the same DNI or the same email address (emails compared case-insensitively). Both checks SHALL be performed before creating a user.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` and email `ana@correo.com` exists and `USER CREATE` is received with DNI `12345678Z` and email `otro@correo.com`
- **THEN** no user is created and a duplicate-DNI error is printed

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is received with DNI `87654321X` and email `ana@correo.com`
- **THEN** no user is created and a duplicate-email error is printed

### Requirement: Delete user
The system SHALL delete a user with `USER DELETE;Correo;Contraseña` only when a user with that email exists and the password matches the stored password; the user SHALL also be removed from every group it belongs to, and the persistent files SHALL then be updated. Otherwise no user SHALL be deleted and an error SHALL be reported.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is received
- **THEN** the user is removed and no longer appears in the persistent file

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is received
- **THEN** the user is not deleted and an authentication error is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is received
- **THEN** no user is deleted and an error is printed

#### Scenario: Deleted user leaves groups
- **WHEN** Ana is a member of group `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** Ana is no longer a member of `G01`, including after a restart

### Requirement: Update user
The system SHALL update one field with `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` only after verifying that a user with the current email exists and the current password matches. `Campo` SHALL be one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. The new value SHALL be validated with the same rules as on creation, and a new email SHALL NOT belong to another user. After a successful update the persistent file SHALL reflect the new state.

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is received for an existing user with matching password
- **THEN** the user's first name becomes `Ana Maria` and the change is persisted

#### Scenario: Update email to an invalid value
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es` is received
- **THEN** the email is not changed and a validation error is printed

#### Scenario: Update email to one used by another user
- **WHEN** another user owns `pedro@correo.com` and `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is received
- **THEN** the email is not changed and a duplicate-email error is printed

#### Scenario: Wrong current password
- **WHEN** `USER UPDATE;ana@correo.com;Bad1!;NAME;Otra` is received
- **THEN** nothing is changed and an authentication error is printed

#### Scenario: DNI cannot be updated
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is received
- **THEN** nothing is changed and an error indicating the field is not editable is printed
