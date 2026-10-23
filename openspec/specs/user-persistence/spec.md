# user-persistence Specification

## Purpose

Keeps users across application runs by storing them in a plain text file in a fixed, human-readable format, without databases or persistence libraries.

## Requirements

### Requirement: User file format
Users SHALL be stored in a text file named `users.txt` in the active data directory: the working directory in normal execution, or the run's temporary directory in `--fly` mode. Each line holds one user's first name, surnames, DNI, email, password hash, phone, and card, separated by `;`. The password field SHALL hold only the BCrypt hash, never the original password.

#### Scenario: Stored line format
- **WHEN** the user Ana (`Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is stored
- **THEN** the file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2` (a BCrypt hash) and ending with `;612345678;1234567890123456`, and the file does not contain `Clave1!`

#### Scenario: Users file in temporary mode
- **WHEN** a user is created during a `--fly` execution
- **THEN** the user is written to `users.txt` inside the run's temporary directory, and the working directory's `users.txt` is not modified

### Requirement: Load users on startup
On startup the application SHALL load every user in the user file; a missing file means no users. A line SHALL be skipped with a warning, without stopping the application, if it is blank or malformed, if its data violates the validation or uniqueness rules, or if its password field is not a BCrypt hash (for example, a plain-text password from an earlier version).

#### Scenario: Users survive restart
- **WHEN** a user is created, the application exits, and the application is started again
- **THEN** the user exists and can be updated or deleted with its credentials

#### Scenario: No file yet
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** it starts normally with no users

#### Scenario: Malformed line skipped
- **WHEN** `users.txt` contains a line with fewer than seven fields
- **THEN** that line is skipped with a warning and every valid line is loaded

#### Scenario: Plain-text password line skipped
- **WHEN** `users.txt` contains a Delivery 1 line with the plain password `Clave1!`
- **THEN** that line is skipped with a warning

### Requirement: Persist every change
Every successful create, delete, or update SHALL be written to the user file before its success message is printed. If the file cannot be written, the application SHALL print an error beginning with `ERROR`, report the operation as failed, and leave the in-memory users as they were before the operation.

#### Scenario: Deletion persisted
- **WHEN** a user is deleted
- **THEN** the user's line is no longer present in `users.txt`

#### Scenario: Write failure
- **WHEN** a create is processed but `users.txt` cannot be written
- **THEN** an error is printed, `USER CREATED` is not printed, and the user does not exist in memory
