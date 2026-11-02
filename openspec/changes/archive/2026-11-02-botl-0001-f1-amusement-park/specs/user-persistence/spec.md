# Spec Delta

## Purpose

Keeps registered users across application executions by storing them in a plain semicolon-separated text file and reloading them at startup.

## ADDED Requirements

### Requirement: Text-file user storage
The system SHALL store all registered users in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number. No database or external persistence library SHALL be used.

#### Scenario: File format
- **WHEN** user Ana (`Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`) has been created
- **THEN** the users file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Storage reflects every successful change
After every successful create, delete, or update, the users file SHALL reflect the current set of users. A rejected operation SHALL leave the file unchanged.

#### Scenario: Persisted updates and deletions
- **WHEN** a valid update or deletion is performed and the application restarts
- **THEN** the reloaded users reflect that operation

#### Scenario: Rejected operation leaves file unchanged
- **WHEN** a create, delete, or update command is rejected
- **THEN** the users file content is identical to its content before the command

### Requirement: Users reloaded at startup
At startup the system SHALL load previously stored users from the users file. A missing file SHALL be treated as having no users; a malformed line SHALL be skipped with a warning without stopping the application.

#### Scenario: Restart restores users
- **WHEN** a valid user was created during one execution and the application is started again
- **THEN** the user is reconstructed with the saved attributes, and creating another user with the same DNI or email is rejected

#### Scenario: No users file yet
- **WHEN** the application starts and the users file does not exist
- **THEN** the application starts with no users and without error
