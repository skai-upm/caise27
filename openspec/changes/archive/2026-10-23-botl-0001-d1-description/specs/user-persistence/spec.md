# Spec Delta

## Purpose

Defines how park users survive between application runs using a plain text file, without databases, persistence frameworks or external libraries.

## ADDED Requirements

### Requirement: User file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card.

#### Scenario: Stored line
- **WHEN** the user Ana Garcia Lopez is created with the example data
- **THEN** the users file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Load at startup
Previously stored users SHALL be loaded when the application starts; if the file does not exist the application SHALL start with no users, and malformed lines SHALL be skipped with a warning rather than aborting startup.

#### Scenario: Users survive a restart
- **WHEN** a user is created in one run and the application is started again
- **THEN** the user exists in the new run (e.g. creating it again is rejected as a duplicate)

#### Scenario: Missing users file
- **WHEN** the application starts and no users file exists
- **THEN** it starts normally with no users

### Requirement: Persist every change
Every successful creation, deletion or modification SHALL be written to the users file before its success message is printed.

#### Scenario: Deletion persisted
- **WHEN** a user is deleted and the application is restarted
- **THEN** the deleted user is not loaded
