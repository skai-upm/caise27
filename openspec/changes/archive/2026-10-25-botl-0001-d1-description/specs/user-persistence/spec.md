# Spec Delta

## Purpose

Defines how users are kept between application runs using a plain text file, without databases, persistence frameworks or external libraries.

## ADDED Requirements

### Requirement: User file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card (e.g. `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`).

#### Scenario: Stored line format
- **WHEN** the user Ana from the creation example is created
- **THEN** the user file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Persist after every change
Every successful user creation, deletion or modification SHALL be reflected in the user file before the next command is processed.

#### Scenario: Deletion is persisted
- **WHEN** a user is deleted
- **THEN** the user's line no longer appears in the user file

### Requirement: Load users at start-up
Previously stored users SHALL be loaded when the application starts so that they are available to every command. A missing user file SHALL be treated as an empty set of users. A malformed line SHALL be skipped with a warning without stopping the application.

#### Scenario: Users survive restarts
- **WHEN** a user is created, the application exits, and the application is started again
- **THEN** `USER DELETE` with that user's email and password succeeds in the new run

#### Scenario: First run without a file
- **WHEN** the application starts and no user file exists
- **THEN** it starts normally with no users
