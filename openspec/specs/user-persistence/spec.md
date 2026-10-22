# user-persistence Specification

## Purpose

Keeps users across application runs by storing them in a plain text file in a fixed, human-readable format, without databases or persistence libraries.

## Requirements

### Requirement: User file format
Users SHALL be stored in a text file named `users.txt` in the working directory. The file holds one user per line, with attributes in the order first name, surnames, DNI, email, password, phone, card, separated by `;`.

#### Scenario: Stored line format
- **WHEN** the user Ana (`Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is stored
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Load users on startup
On startup the application SHALL load all users stored in the user file. A missing file SHALL be treated as no users. A line that is blank, malformed, or whose data violates the validation or uniqueness rules SHALL be skipped with a warning, without stopping the application.

#### Scenario: Users survive restart
- **WHEN** a user is created, the application exits, and the application is started again
- **THEN** the user exists and can be updated or deleted with its credentials

#### Scenario: No file yet
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** it starts normally with no users

#### Scenario: Malformed line skipped
- **WHEN** `users.txt` contains a line with fewer than seven fields
- **THEN** that line is skipped with a warning and every valid line is loaded

### Requirement: Persist every change
Every successful create, delete, or update SHALL be written to the user file before its success message is printed. If the file cannot be written, the application SHALL print an error beginning with `ERROR`, report the operation as failed, and leave the in-memory users as they were before the operation.

#### Scenario: Deletion persisted
- **WHEN** a user is deleted
- **THEN** the user's line is no longer present in `users.txt`

#### Scenario: Write failure
- **WHEN** a create is processed but `users.txt` cannot be written
- **THEN** an error is printed, `USER CREATED` is not printed, and the user does not exist in memory
