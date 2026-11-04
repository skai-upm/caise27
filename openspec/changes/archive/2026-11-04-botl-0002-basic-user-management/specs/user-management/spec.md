# Spec Delta

## Purpose

Defines the text commands that create, delete and update users, how they are parsed, authenticated and rejected, and how identity uniqueness is enforced.

## ADDED Requirements

### Requirement: Command parsing
The application SHALL read text commands from the terminal, one per line, and split each command into its components with `String.split` on `;`. The first component SHALL name the operation (`USER CREATE`, `USER DELETE` or `USER UPDATE`). Each command SHALL produce exactly one result line starting with `OK` on success or `ERROR` on failure.

#### Scenario: Unknown operation
- **WHEN** the command `USER RENAME;ana@correo.com` is processed
- **THEN** the result starts with `ERROR`, no user data changes and the application keeps accepting commands

#### Scenario: Wrong number of parameters
- **WHEN** a `USER CREATE`, `USER DELETE` or `USER UPDATE` command has fewer or more parameters than its format requires
- **THEN** the result starts with `ERROR`, no user is created or changed and the application keeps accepting commands

#### Scenario: Blank line
- **WHEN** an empty or blank line is read
- **THEN** it is ignored without changing any data

### Requirement: Create user
The system SHALL process `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` by validating every field before storing anything, and SHALL store the user only if all fields are valid and neither its DNI nor its email belongs to an existing user. Email comparison SHALL ignore letter case.

#### Scenario: Valid creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no user has that DNI or email
- **THEN** the result starts with `OK` and the user is stored

#### Scenario: Any invalid field
- **WHEN** a creation has an invalid DNI, email, password or card number, or a blank name, surname or phone
- **THEN** the result starts with `ERROR` and no user is stored

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` and email `ana@correo.com` exists and a creation with DNI `12345678Z` and email `otro@correo.com` is processed
- **THEN** the creation is rejected with `ERROR`

#### Scenario: Duplicate email
- **WHEN** a user with DNI `12345678Z` and email `ana@correo.com` exists and a creation with DNI `87654321X` and email `ana@correo.com` is processed
- **THEN** the creation is rejected with `ERROR`

### Requirement: Delete user
The system SHALL process `USER DELETE;Correo;Contraseña` by finding the user with that email, checking that the password matches the stored password, removing the user and updating the persistent file. If the email is unknown or the password does not match, nothing SHALL be deleted.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** the result starts with `OK` and the user no longer exists, also after a restart

#### Scenario: Wrong password or unknown email
- **WHEN** a deletion supplies a wrong password or an email that no user has
- **THEN** the result starts with `ERROR` and all users remain stored

### Requirement: Update user
The system SHALL process `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` (case-insensitive). It SHALL authenticate with the current email and password, validate the new value with the same rules as creation, reject a new email that belongs to another user, and on success update the persistent file.

#### Scenario: Update each editable field
- **WHEN** an authenticated update sets `NAME` to `Ana Maria`, `SURNAME`, `PHONE`, `EMAIL` to `ana.nueva@correo.com`, `PASSWORD` to `Nueva2#` or `CARD` to `9999888877776666`
- **THEN** the result starts with `OK` and the stored user reflects the new value, also after a restart

#### Scenario: Subsequent authentication uses the new credentials
- **WHEN** the email has been changed to `ana.nueva@correo.com` and then the password to `Nueva2#`
- **THEN** `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` succeeds and requests using the old email or old password fail

#### Scenario: Unauthorized update
- **WHEN** an update supplies a wrong password or an unknown email
- **THEN** the result starts with `ERROR` and no stored value changes

#### Scenario: Invalid new value
- **WHEN** an authenticated update supplies an email already used by another user, an invalid email, an invalid password or an invalid card number
- **THEN** the result starts with `ERROR` and the existing value is kept

#### Scenario: Non-editable or unknown field
- **WHEN** an authenticated update names the field `DNI` or an unknown field
- **THEN** the result starts with `ERROR` and the user is unchanged
