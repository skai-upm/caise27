# user-file-persistence Specification

## Purpose
Defines how users are stored in a semicolon-separated text file and recovered when the application starts again.

## Requirements

### Requirement: User file format
The system SHALL store users in a text file with one user per line and fields in the order `Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`.

#### Scenario: Stored line
- **WHEN** the user `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` has been created
- **THEN** the user file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: File updated after every successful change
After every successful create, update or delete the user file SHALL reflect exactly the current set of users, and a rejected command SHALL leave it unchanged.

#### Scenario: Deletion removes the line
- **WHEN** a user is deleted
- **THEN** that user's line no longer appears in the file

#### Scenario: Rejected command leaves file intact
- **WHEN** a command is rejected
- **THEN** the file content is the same as before the command

### Requirement: Users recovered at startup
On startup the system SHALL load all users from the user file, if it exists, so that they can be authenticated, updated, deleted and counted for uniqueness; a missing file SHALL mean no users.

#### Scenario: Restart recovers current state
- **GIVEN** users were created, one updated and one deleted, and the application stopped
- **WHEN** the application starts again with the same user file
- **THEN** exactly the remaining users are present with their latest values

#### Scenario: First start without a file
- **GIVEN** no user file exists
- **WHEN** the application starts
- **THEN** it starts with no users and does not fail
