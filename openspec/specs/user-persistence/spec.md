# user-persistence Specification

## Purpose

Keeps registered users between application runs using a plain text file, without databases or persistence libraries.

## Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file `users.txt` in the data directory (the working directory, or the temporary directory in `--fly` mode) with one user per line and attributes in the order first name, surnames, DNI, email, BCrypt password hash, phone, card, separated by `;`.

#### Scenario: Stored line
- **WHEN** Ana Garcia Lopez is created with DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678`, card `1234567890123456`
- **THEN** the data file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`

#### Scenario: Temporary mode location
- **WHEN** the application runs with `--fly` and a user is created
- **THEN** the line is written to `users.txt` inside the temporary directory and the working directory's `users.txt` is untouched

### Requirement: Save after every change
Every successful creation, deletion or modification SHALL be written to the data file before the next command is processed.

#### Scenario: Deletion persisted
- **WHEN** a user is deleted
- **THEN** the data file no longer contains that user's line

### Requirement: Load at startup
Previously stored users SHALL be loaded when the application starts. A missing data file SHALL be treated as an empty user list, and malformed lines SHALL be skipped without stopping the application.

#### Scenario: Data survives restart
- **WHEN** a user is created, the application exits, and it is started again
- **THEN** creating a user with the same DNI is rejected as a duplicate

#### Scenario: First run
- **WHEN** the application starts and no data file exists
- **THEN** it starts normally with no users
