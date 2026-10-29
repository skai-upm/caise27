# Spec Delta

## Purpose

Defines how users are kept between application runs in a plain text file, handled by a component separate from the user model and the entry point.

## ADDED Requirements

### Requirement: Text file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number. Databases, persistence frameworks and external persistence libraries MUST NOT be used.

#### Scenario: Stored line format
- **WHEN** Ana is created with `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Load users at start-up
Previously stored users SHALL be loaded when the application starts, so that users created, modified or deleted in a previous run are reflected in the next run. A missing users file SHALL be treated as an empty set of users.

#### Scenario: Users survive restart
- **WHEN** Ana is created in one run and the application is started again
- **THEN** Ana exists and creating another user with DNI `12345678Z` is rejected as duplicate

#### Scenario: First run without file
- **WHEN** the application starts and no users file exists
- **THEN** it starts with no users and without error

### Requirement: Changes are persisted
Every successful creation, modification or deletion SHALL be reflected in the users file.

#### Scenario: Deletion persisted
- **WHEN** a user is deleted and the application is restarted
- **THEN** the deleted user is not loaded

#### Scenario: Modification persisted
- **WHEN** a user's telephone is updated and the application is restarted
- **THEN** the loaded user has the new telephone

### Requirement: Separation of persistence
File reading and writing SHALL be implemented in a component separate from the user model and from the application entry point.

#### Scenario: Persistence isolated
- **WHEN** the source code is inspected
- **THEN** no file I/O for users appears in the user class or the main class
