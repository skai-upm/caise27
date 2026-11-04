# user-file-persistence Specification

## Purpose

Defines how users are stored in a plain text file and recovered when the application starts, using only the Java standard library.

## Requirements

### Requirement: Semicolon-separated user records
The system SHALL store each user on one line of a text file with fields in the order `Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`.

#### Scenario: File content after creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: File reflects every successful change
After every successful create, update or delete, the file SHALL contain exactly the current set of users and their current values; rejected commands SHALL leave the file unchanged.

#### Scenario: Rejected command leaves file unchanged
- **GIVEN** a persisted file with one user
- **WHEN** an invalid create, update or delete is processed
- **THEN** the file content is identical to before

### Requirement: Restart recovery
On startup the system SHALL load all users from the persistence file, so that a new process using the same file sees exactly the users and values that were current when the previous process stopped. A missing file SHALL be treated as no users.

#### Scenario: Restart after create, update and delete
- **GIVEN** two users were created, one was updated and the other deleted
- **WHEN** a new process starts with the same file
- **THEN** only the remaining user is recovered, with its updated values

#### Scenario: First start without file
- **GIVEN** no persistence file exists
- **WHEN** the application starts
- **THEN** it starts with no users and without error
