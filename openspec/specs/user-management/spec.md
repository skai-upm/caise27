# user-management Specification

## Purpose

Defines how users are created, deleted and updated through text commands, including uniqueness of DNI and email and authentication with the current credentials.

## Requirements

### Requirement: Create user
The system SHALL process `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` by validating every value and checking uniqueness before storing anything, and SHALL store the user only when all checks pass.

#### Scenario: Valid creation
- **GIVEN** no existing users
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is stored, a success message is shown and the user is present after restart

#### Scenario: Invalid data is not stored
- **WHEN** a create command has an invalid DNI (`12345678A`), email (`ana@correo.es`), password (`clave1!`) or card (`123456789012345`)
- **THEN** creation is rejected with an error message and no user is stored or persisted

### Requirement: Unique DNI and email
The system SHALL reject creating a user whose DNI or whose email (compared case-insensitively) already belongs to an existing user.

#### Scenario: Duplicate DNI
- **GIVEN** a user `12345678Z` / `ana@correo.com`
- **WHEN** a user with DNI `12345678Z` and email `otro@correo.com` is created
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **GIVEN** a user `12345678Z` / `ana@correo.com`
- **WHEN** a user with DNI `87654321X` and email `ana@correo.com` is created
- **THEN** creation is rejected

### Requirement: Authenticated deletion
The system SHALL process `USER DELETE;Correo;Contraseña` by locating the user by email, verifying the password matches, removing the user and updating the persistent file; it SHALL NOT delete anything when the email does not exist, the password is wrong, or the user has any ticket dated on or after the configured system date. Tickets dated before the system date SHALL NOT, by themselves, prevent deletion.

#### Scenario: Successful deletion
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is removed and is absent after restart

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** the command is rejected and stored users are unchanged

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and `TICKET CREATE;T001;ana@correo.com;2026-10-15`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed, and again after `SYSTEM DATE;2026-10-15`
- **THEN** both deletions are rejected and Ana remains registered

#### Scenario: Past ticket alone does not block
- **GIVEN** Ana's only ticket `T001` is dated `2026-10-15`
- **WHEN** `SYSTEM DATE;2026-10-16` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** Ana is deleted and ticket `T001` is retained as history

#### Scenario: Ticket inside a group ticket blocks deletion
- **GIVEN** system date `2026-10-01` and group ticket `TG01` dated `2026-10-15` containing a ticket for Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is rejected and Ana remains registered

### Requirement: Authenticated update of editable fields
The system SHALL process `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (an enumerated set); it SHALL verify the current email and password first, revalidate the new value with the field's rules, and persist the new state on success.

#### Scenario: Update each editable field
- **GIVEN** user `ana@correo.com` / `Clave1!`
- **WHEN** `NAME` → `Ana Maria`, `SURNAME` → `Garcia Ruiz`, `PHONE` → `699999999`, `EMAIL` → `ana.nueva@correo.com`, `PASSWORD` → `Nueva2#` and `CARD` → `9999888877776666` are updated in sequence using the current credentials each time
- **THEN** each update succeeds and the persisted user reflects all new values after restart

#### Scenario: New credentials take effect
- **GIVEN** the email was changed to `ana.nueva@correo.com` and the password to `Nueva2#`
- **WHEN** an update or delete uses the old email or old password
- **THEN** it is rejected, while the new credentials are accepted

#### Scenario: Invalid update rejected
- **WHEN** an update sets an email used by another user, an email with invalid format, a password missing a category, or a card that is not 16 digits
- **THEN** the update is rejected and the existing value is unchanged

#### Scenario: Unauthorized or unknown field
- **WHEN** the current password is wrong, the email does not exist, or the field is not one of the enumerated values
- **THEN** the update is rejected and stored data is unchanged
