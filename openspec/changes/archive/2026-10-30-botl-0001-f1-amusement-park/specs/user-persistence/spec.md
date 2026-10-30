# Spec Delta

## Purpose

Keeps users between runs of the application by storing them in a plain text file and loading them at startup.

## ADDED Requirements

### Requirement: Text file format
Users SHALL be stored in a text file, one user per line, with attributes in the order first name, surnames, DNI, email, password, phone, card separated by `;`.

#### Scenario: Stored line format
- **WHEN** the user Ana Garcia Lopez is created
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Save on every change
After every successful create, delete or update, the persistent file SHALL reflect the current set of users.

#### Scenario: Data survives restart
- **WHEN** a user is created, the application exits and is started again
- **THEN** the user exists and is subject to uniqueness checks

### Requirement: Load at startup
Previously stored users SHALL be loaded when the application starts. A missing file SHALL be treated as no users. Lines that do not have exactly seven fields SHALL be skipped with a warning, without terminating the application.

#### Scenario: No file yet
- **WHEN** the application starts and the users file does not exist
- **THEN** it starts with no users and without error

#### Scenario: Malformed line
- **WHEN** the users file contains a line with fewer than seven fields
- **THEN** that line is ignored with a warning and other users are loaded
