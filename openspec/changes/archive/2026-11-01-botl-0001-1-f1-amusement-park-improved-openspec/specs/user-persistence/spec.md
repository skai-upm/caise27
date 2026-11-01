# Spec Delta

## Purpose

Keeps registered users across executions by storing them in a plain text file and reconstructing them when the application starts.

## ADDED Requirements

### Requirement: Semicolon-separated user file
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number. No database or external persistence library SHALL be used.

#### Scenario: File format
- **GIVEN** user Ana Garcia Lopez with DNI `12345678Z` has been created
- **WHEN** the user file is read
- **THEN** it contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Users restored at startup
On startup the application SHALL load all previously stored users from the user file; a missing file SHALL be treated as no users.

#### Scenario: Restart restores users
- **GIVEN** a valid user was created during one execution
- **WHEN** the application terminates and starts again
- **THEN** the user is reconstructed with the saved attributes and its DNI and email remain reserved

#### Scenario: First run without file
- **GIVEN** no user file exists
- **WHEN** the application starts
- **THEN** it starts with no users and without error

### Requirement: Storage reflects every successful change
After every successful creation, deletion or update the user file SHALL reflect the current set of users; failed operations SHALL leave the file unchanged.

#### Scenario: Persisted update and deletion
- **GIVEN** a stored user
- **WHEN** a valid update or deletion is performed and the application restarts
- **THEN** the loaded state reflects that operation

#### Scenario: Failed operation leaves file intact
- **WHEN** a creation, deletion or update is rejected
- **THEN** the user file content is unchanged
