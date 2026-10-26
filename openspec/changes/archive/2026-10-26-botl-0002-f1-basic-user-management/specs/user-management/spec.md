# Spec Delta

## Purpose

Manages the park's registered users: creation, update and deletion through text commands, data validation, uniqueness rules and persistent storage between executions.

## ADDED Requirements

### Requirement: User data
Each user SHALL store first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created.

#### Scenario: Stored attributes
- **WHEN** a user is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the stored user has exactly those seven values

### Requirement: Create user
The system SHALL create a user with `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` only when every value is valid and no uniqueness rule is violated; otherwise no user SHALL be stored.

#### Scenario: Valid creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** the user is created and persisted

#### Scenario: Invalid data rejects creation
- **WHEN** a `USER CREATE` command has any invalid or empty value
- **THEN** an error is reported and no user is stored

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one uppercase letter equal to the letter at position `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`.

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
An email SHALL be valid only when it has the form `texto@texto.xxx`: exactly one `@`, a non-empty part before it, and after it a non-empty domain, a period, and an extension of exactly three characters. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone number is stored as given
The telephone number SHALL be stored as received (non-empty) without encryption or format processing.

#### Scenario: Phone stored
- **WHEN** a user is created with phone `612345678`
- **THEN** the stored phone is `612345678`

### Requirement: Unique DNI and email
The system SHALL reject creating a user whose DNI or email address already belongs to another user. Email comparison SHALL ignore letter case.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** creation is rejected

### Requirement: Delete user
The system SHALL delete a user with `USER DELETE;Correo;Contraseña` only when a user with that email exists and the password matches the stored one; the persistent file SHALL then be updated.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and the credentials match
- **THEN** the user is removed and no longer appears in the persistent file

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE` is processed with an unknown email or a wrong password
- **THEN** an error is reported and no user is deleted

### Requirement: Update user
The system SHALL update one field with `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` after verifying the current email and password. `Campo` SHALL be one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`; the DNI SHALL NOT be updatable. After success the persistent file SHALL reflect the new state.

#### Scenario: Change first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the user's first name becomes `Ana Maria` and is persisted

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with a wrong password
- **THEN** an error is reported and nothing changes

#### Scenario: DNI cannot be updated
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is reported and the DNI is unchanged

### Requirement: Updated values are revalidated
When `USER UPDATE` changes a restricted field, the new value SHALL satisfy the same rules as on creation: a new email SHALL have a valid format and SHALL NOT belong to another user, a new password SHALL meet the password rules, and a new card number SHALL contain exactly 16 digits.

#### Scenario: New email already used
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed and `pedro@correo.com` belongs to another user
- **THEN** the update is rejected

#### Scenario: Invalid new password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave` is processed
- **THEN** the update is rejected and the old password remains valid

### Requirement: Persistent user storage
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, phone, card. Users stored in the file SHALL be loaded when the application starts.

#### Scenario: Users survive a restart
- **WHEN** a user is created, the application terminates and is started again
- **THEN** the previously created user exists and can be updated or deleted

#### Scenario: File format
- **WHEN** Ana is created with the example data
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
