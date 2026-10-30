# Spec Delta

## Purpose

Stores registered users durably in a plain text file and restores them when the application starts, without databases or external persistence libraries.

## ADDED Requirements

### Requirement: Text-file user storage
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card. Reading and writing SHALL be implemented in a component independent of the user representation and the main application class. No database or external persistence library SHALL be used.

#### Scenario: File format
- **GIVEN** user Ana was created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **WHEN** the users file is inspected
- **THEN** it contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Load users at startup
The application SHALL reconstruct previously stored users from the text file when it starts. A missing file SHALL be treated as an empty user set.

#### Scenario: Restart restores users
- **GIVEN** a valid user was created during a previous execution
- **WHEN** the application starts again
- **THEN** the user is available with the saved attributes and still blocks duplicate DNI/email

#### Scenario: First start without file
- **GIVEN** no users file exists
- **WHEN** the application starts
- **THEN** it starts with no users and without error

### Requirement: Persist every successful change
After every successful creation, deletion or update, the users file SHALL reflect the new state. Failed operations SHALL leave the file unchanged.

#### Scenario: Persisted update and deletion
- **GIVEN** a stored user
- **WHEN** a valid update or deletion is performed and the application restarts
- **THEN** the loaded state reflects that operation
