# user-persistence Specification

## Purpose

Keeps registered users across application runs by storing them in a plain text file and reloading them at startup.

## Requirements

### Requirement: Text file format
Users SHALL be stored in a text file with one user per line, attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number. No database or external persistence library SHALL be used.

#### Scenario: Stored line format
- **WHEN** the user Ana Garcia Lopez (`12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is stored
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Persist on change
After every successful create, delete or update, the users file SHALL reflect the complete current set of users.

#### Scenario: File updated after deletion
- **WHEN** a user is deleted successfully
- **THEN** the users file no longer contains that user's line

### Requirement: Load at startup
When the application starts, it SHALL load all users stored in the users file. If the file does not exist, the application SHALL start with no users. Malformed lines SHALL be skipped without terminating the application.

#### Scenario: Users recovered after restart
- **WHEN** a user was created in a previous run and the application starts again
- **THEN** the user exists and uniqueness checks and credentials apply to it

#### Scenario: Missing file
- **WHEN** the application starts and no users file exists
- **THEN** it starts with an empty user set and no error terminates it
