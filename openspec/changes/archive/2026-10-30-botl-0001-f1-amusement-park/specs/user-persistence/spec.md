# Spec Delta

## Purpose

Defines how users are preserved between runs of the application using a plain text file, without databases or persistence libraries.

## ADDED Requirements

### Requirement: Text file storage format
Users SHALL be stored in the text file `users.txt` in the working directory, one user per line, with attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card.

#### Scenario: Stored line format
- **WHEN** user Ana Garcia Lopez (DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678`, card `1234567890123456`) is created
- **THEN** `users.txt` contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Changes are persisted
Every successful create, delete or update operation SHALL be reflected in the users file before the next command is processed.

#### Scenario: Data survives a restart
- **WHEN** a user is created, the application exits and is started again
- **THEN** the user exists in the new run and duplicate creation is rejected

### Requirement: Users are loaded at start-up
Previously stored users SHALL be loaded when the application starts. A missing users file SHALL be treated as an empty set of users.

#### Scenario: No users file yet
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** the application starts normally with no users
