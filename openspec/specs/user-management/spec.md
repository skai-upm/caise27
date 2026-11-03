# user-management Specification

## Purpose

Defines the text-command contract through which terminal users create, update and delete user accounts, including uniqueness and authentication rules.

## Requirements

### Requirement: Text command interpretation
The application SHALL read commands as text lines from the active input source (the terminal, or the command file given as first launch argument), split them into components with `String.split` on `;` (the first component holding the operation words separated by a space), and SHALL report invalid operations or wrong parameter counts as errors without terminating.

#### Scenario: Unknown operation
- **WHEN** `USER RENAME;ana@correo.com` is processed
- **THEN** an error is reported, no user changes, and the application keeps accepting commands

#### Scenario: Insufficient parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** an error is reported and no user is created

#### Scenario: Same interpretation from a file
- **GIVEN** a command file containing `USER CREATE;Ana;Garcia Lopez`
- **WHEN** the application runs with that file
- **THEN** the same error is reported as when the line is typed in the terminal and no user is created

### Requirement: User creation
The system SHALL process `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` by validating every field before storing anything; if any field is invalid the user SHALL NOT be stored.

#### Scenario: Valid creation
- **GIVEN** no existing users
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** a success response is returned and the user is stored

#### Scenario: Invalid field blocks creation
- **WHEN** a creation request contains DNI `12345678A`, email `ana@correo.es`, password `Clave12` or card `123456789012345`
- **THEN** an error is reported and no user is stored

### Requirement: User uniqueness
The system SHALL reject a creation whose DNI or email address already belongs to an existing user. Email comparison SHALL be case-insensitive.

#### Scenario: Duplicate DNI
- **GIVEN** a user `12345678Z;ana@correo.com` exists
- **WHEN** a user with DNI `12345678Z` and email `otro@correo.com` is created
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **GIVEN** a user `12345678Z;ana@correo.com` exists
- **WHEN** a user with DNI `87654321X` and email `ana@correo.com` is created
- **THEN** creation is rejected

### Requirement: Authenticated deletion
The system SHALL process `USER DELETE;Correo;Contraseña` by locating the user by email, verifying the password matches the stored one, and only then removing the user.

#### Scenario: Successful deletion
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user no longer exists

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error is reported and no user is deleted

### Requirement: Authenticated update
The system SHALL process `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`, by first verifying the current email and password, then validating the new value with the same rules as creation, and only then applying it.

#### Scenario: Update each editable field
- **GIVEN** user `ana@correo.com` / `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed (and analogously for SURNAME, PHONE)
- **THEN** the field holds the new value

#### Scenario: Chained credential updates
- **GIVEN** user `ana@correo.com` / `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** each succeeds and the user has email `ana.nueva@correo.com`, password `Nueva2#` and card `9999888877776666`

#### Scenario: Invalid new value
- **WHEN** an update sets an email already used by another user, an invalid email, a password missing a category, or a card that is not 16 digits
- **THEN** an error is reported and the existing value is unchanged

#### Scenario: Failed authentication
- **WHEN** an update supplies an unknown email or a wrong password
- **THEN** an error is reported and no data changes

#### Scenario: Unsupported field
- **WHEN** an update names a field outside the editable set (for example `DNI`)
- **THEN** an error is reported and no data changes
