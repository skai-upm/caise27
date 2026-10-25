# user-persistence Specification

## Purpose

Defines how users are persisted in a semicolon-separated text file and restored when the application starts.

## Requirements

### Requirement: Users stored in a text file
Users SHALL be stored in a text file, one user per line, with the attributes first name, surname(s), DNI, email, password hash, telephone and card separated by `;` in that order. The password SHALL be stored as a BCrypt hash, never in plaintext. No database or external persistence library SHALL be used.

#### Scenario: File content after creation
- **WHEN** user Ana is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash of `Clave1!`, then `;612345678;1234567890123456`

### Requirement: Storage reflects every change
After every successful create, delete or update, the users file SHALL reflect the current set of users and their state.

#### Scenario: Update persisted
- **WHEN** a user's telephone is updated to `699999999`
- **THEN** that user's line in the file contains `699999999`

### Requirement: Users restored at startup
When the application starts, it SHALL load all users previously stored in the file; a missing file SHALL be treated as no users, and malformed lines SHALL be skipped with a warning instead of aborting startup.

#### Scenario: Restart restores users
- **WHEN** a user is created, the application is terminated and started again
- **THEN** the user exists and can be deleted with its email and password

#### Scenario: No file yet
- **WHEN** the application starts and the users file does not exist
- **THEN** it starts with no users and without errors
