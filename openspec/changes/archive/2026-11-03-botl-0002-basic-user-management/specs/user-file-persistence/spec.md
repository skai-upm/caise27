# Spec Delta

## Purpose

Keeps registered users across application runs by storing them in a plain semicolon-separated text file.

## ADDED Requirements

### Requirement: User file format
The system SHALL store users in a text file, one user per line, with the fields `Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` separated by `;`.

#### Scenario: Line written for a created user
- **WHEN** user Ana from the creation example is created
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Save after successful changes
After every successful creation, update or deletion the file SHALL reflect the current set of users; rejected commands SHALL leave the file unchanged.

#### Scenario: Rejected command leaves file intact
- **GIVEN** a file with one user
- **WHEN** an invalid creation or an unauthorized deletion is processed
- **THEN** the file content is unchanged

### Requirement: Reload at startup
On startup the system SHALL load all users from the file, so that previously stored users can be authenticated, updated and deleted and their DNI/email remain unique. A missing file SHALL mean no users.

#### Scenario: Restart recovers current state
- **GIVEN** users were created, one updated and one deleted in a previous run
- **WHEN** the application restarts with the same file
- **THEN** exactly the remaining users are loaded, with their latest values

#### Scenario: No file yet
- **GIVEN** no user file exists
- **WHEN** the application starts
- **THEN** it starts with no users and without error
