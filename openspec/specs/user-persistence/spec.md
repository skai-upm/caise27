# user-persistence Specification

## Purpose

Defines how registered users are stored in a plain text file and reconstructed when the application starts, without databases or persistence libraries.

## Requirements

### Requirement: Semicolon-separated user file
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card. No database or external persistence library SHALL be used.

#### Scenario: File format after creation
- **WHEN** user Ana is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the user file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Persistence after every change
The user file SHALL be rewritten to reflect the current set of users after every successful creation, deletion or update. Failed operations SHALL leave the file unchanged.

#### Scenario: Persisted update and deletion
- **WHEN** a stored user is successfully updated or deleted
- **THEN** the user file reflects the updated state or no longer contains that user

#### Scenario: Failed operation leaves file intact
- **WHEN** a creation, update or deletion is rejected
- **THEN** the user file content is unchanged

### Requirement: Users restored at startup
On startup the application SHALL load all users previously stored in the user file. A missing file SHALL be treated as an empty user set.

#### Scenario: Restart restores users
- **WHEN** a user is created, the application terminates and the application is started again
- **THEN** the user is reconstructed from the file with all saved attributes, and creating another user with the same DNI or email is rejected

#### Scenario: First run without file
- **WHEN** the application starts and no user file exists
- **THEN** it starts with no users and without reporting an error
