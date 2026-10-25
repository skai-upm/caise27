# Spec Delta

## Purpose

Manages park users: their data, validation rules, uniqueness, creation, deletion, update and persistent storage between executions.

## ADDED Requirements

### Requirement: User data
Each user SHALL store first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created.

#### Scenario: User is created with all fields
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is executed with no existing users
- **THEN** the system reports `OK USER CREATED` and a user with exactly those seven values exists

### Requirement: Create user command
The system SHALL accept `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, validate every value before creating the user, and store nothing when any value is invalid or missing. First name, surname(s) and telephone SHALL be non-blank.

#### Scenario: Wrong parameter count
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is executed
- **THEN** the system reports an error and no user is created

#### Scenario: Invalid data
- **WHEN** a `USER CREATE` command contains an invalid DNI, email, password or card number
- **THEN** the system reports an error identifying the invalid field and no user is created

#### Scenario: Blank name
- **WHEN** a `USER CREATE` command has an empty first name
- **THEN** the system reports an error and no user is created

### Requirement: DNI validation
A DNI SHALL consist of eight digits followed by one letter, and the letter SHALL match the eight digits according to the Spanish DNI check algorithm (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`). The letter MAY be given in lowercase and SHALL be stored in uppercase.

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
An email SHALL have the form `texto@texto.xxx`: exactly one `@`, a non-empty part before it, and after it a non-empty domain name, a period and an extension of exactly three characters. Whitespace SHALL NOT be allowed. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each one is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** each one is rejected

### Requirement: Card number validation
A card number SHALL contain exactly 16 consecutive digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** each one is rejected

### Requirement: User uniqueness
Two users SHALL NOT share the same DNI, and two users SHALL NOT share the same email address (compared case-insensitively). Both checks SHALL happen before creating a user.

#### Scenario: Duplicate DNI
- **WHEN** user `12345678Z;ana@correo.com` exists and `USER CREATE` is executed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** the system reports an error and no user is created

#### Scenario: Duplicate email
- **WHEN** user `12345678Z;ana@correo.com` exists and `USER CREATE` is executed with DNI `87654321X` and email `ana@correo.com`
- **THEN** the system reports an error and no user is created

### Requirement: Delete user command
The system SHALL accept `USER DELETE;Correo;Contraseña`, find the user by email, check that the password matches, delete the user and update the persistent storage. If the email does not exist or the password is wrong, no user SHALL be deleted.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** the system reports `OK USER DELETED` and the user no longer exists, also after restarting the application

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is executed for a user whose password is `Clave1!`
- **THEN** the system reports an error and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is executed and no such user exists
- **THEN** the system reports an error and no user is deleted

### Requirement: Update user command
The system SHALL accept `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, authenticate the user by current email and password before changing anything, and update one editable field: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD`. The field name SHALL be case-insensitive. The DNI SHALL NOT be editable.

#### Scenario: Update first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is executed with valid credentials
- **THEN** the system reports `OK USER UPDATED` and the user's first name is `Ana Maria`

#### Scenario: Update email then password then card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are executed
- **THEN** each command succeeds and the user has the new email, password and card number

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is executed with an unknown email or a wrong password
- **THEN** the system reports an error and nothing changes

#### Scenario: DNI is not editable
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is executed
- **THEN** the system reports an error and the DNI is unchanged

#### Scenario: Unknown field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is executed
- **THEN** the system reports an error and nothing changes

### Requirement: Update re-validation
When updating a field with restrictions, the new value SHALL be validated with the same rules as on creation: a new email SHALL have a valid format and SHALL NOT belong to another user, a new password SHALL meet the password rules, a new card SHALL contain exactly 16 digits, and new name, surname or telephone values SHALL be non-blank.

#### Scenario: Invalid new email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es` is executed
- **THEN** the system reports an error and the email is unchanged

#### Scenario: Email of another user
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is executed
- **THEN** the system reports an error and the email is unchanged

#### Scenario: Invalid new password or card
- **WHEN** `USER UPDATE` sets `PASSWORD` to `clave1!` or `CARD` to `123`
- **THEN** the system reports an error and the value is unchanged

### Requirement: User persistence
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card. The file SHALL be rewritten after every successful create, delete or update, and users stored previously SHALL be loaded when the application starts. No database or external persistence library SHALL be used.

#### Scenario: Users survive a restart
- **WHEN** two users are created, the application is stopped and started again
- **THEN** both users exist with the same data, and creating another user with one of their DNIs or emails is rejected

#### Scenario: File format
- **WHEN** user Ana from the creation example is stored
- **THEN** the storage file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

#### Scenario: Malformed stored line
- **WHEN** the storage file contains a line that does not have seven fields
- **THEN** that line is skipped with a warning and the remaining users are loaded
