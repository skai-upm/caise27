# Spec Delta

## Purpose

Defines how park users are registered, validated, updated, deleted and persisted between executions of the application.

## ADDED Requirements

### Requirement: User data
Each user SHALL have a first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created.

#### Scenario: User fields stored
- **WHEN** a user is created with all seven fields
- **THEN** the stored user holds exactly those seven values

### Requirement: Create user
The system SHALL create a user with `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` only if every field is non-empty and valid and uniqueness holds; otherwise no user SHALL be stored and an `ERROR` line explaining the first failing rule SHALL be printed. On success it SHALL print `OK USER CREATED`.

#### Scenario: Valid creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is stored and `OK USER CREATED` is printed

#### Scenario: Invalid data
- **WHEN** a `USER CREATE` command has an invalid DNI, email, password or card number
- **THEN** no user is stored and an `ERROR` line is printed

### Requirement: DNI validation
A DNI SHALL be valid only if it has exactly eight digits followed by one uppercase letter equal to the standard Spanish control letter (`TRWAGMYFPDXBNJZSQVHLCKE` indexed by the number modulo 23).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected even though its format is correct

### Requirement: Email validation
An email SHALL be valid only if it contains exactly one `@`, a non-empty part before it, and after it a non-empty domain name, a period, and an extension of exactly three characters, with no whitespace.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace).

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing character class
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Unique DNI and email
The system SHALL reject creating a user whose DNI or email already belongs to an existing user.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** the user is not created and an `ERROR` line is printed

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** the user is not created and an `ERROR` line is printed

### Requirement: Delete user
The system SHALL delete a user with `USER DELETE;Correo;Contraseña` only when a user with that email exists and the password matches the stored one, printing `OK USER DELETED`; otherwise it SHALL print an `ERROR` line and keep the user.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed for an existing user with that password
- **THEN** the user is removed, the file is updated and `OK USER DELETED` is printed

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE` is processed with an unknown email or a wrong password
- **THEN** no user is deleted and an `ERROR` line is printed

### Requirement: Update user
The system SHALL update one field with `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` after checking the current email and password. `Campo` SHALL be one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`; the DNI SHALL NOT be updatable. On success it SHALL print `OK USER UPDATED`.

#### Scenario: Change first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** the first name becomes `Ana Maria` and `OK USER UPDATED` is printed

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with an unknown email or a wrong password
- **THEN** nothing changes and an `ERROR` line is printed

#### Scenario: DNI not editable
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** nothing changes and an `ERROR` line reports an invalid field

### Requirement: Update revalidation
A new value for a restricted field SHALL be validated before it is applied: a new email SHALL meet the email rules and not belong to another user, a new password SHALL meet the password rules, and a new card number SHALL meet the card rules. Any field's new value SHALL be non-empty.

#### Scenario: Email already used
- **WHEN** a user updates their EMAIL to an email belonging to another user
- **THEN** nothing changes and an `ERROR` line is printed

#### Scenario: Weak new password
- **WHEN** a user updates their PASSWORD to `clave1!`
- **THEN** nothing changes and an `ERROR` line is printed

### Requirement: User persistence
Users SHALL be stored in a text file with one user per line and fields separated by `;` in the order name, surname(s), DNI, email, password, telephone, card. The file SHALL be rewritten after every successful create, delete or update, and users stored in it SHALL be loaded when the application starts.

#### Scenario: Users survive a restart
- **WHEN** a user is created, the application is stopped and started again
- **THEN** the user exists and a new `USER CREATE` with the same DNI is rejected

#### Scenario: File reflects updates
- **WHEN** a user's email is changed with `USER UPDATE`
- **THEN** the user's line in the file contains the new email
