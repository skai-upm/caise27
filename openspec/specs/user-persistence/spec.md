# user-persistence Specification

## Purpose

Defines how users are persisted between executions in a plain semicolon-separated text file, without databases or external persistence libraries.

## Requirements

### Requirement: Users file format
Users SHALL be stored in a text file (`data/users.txt` by default), one user per line, with the attributes in the order first name, surname(s), DNI, email, password hash, telephone, card number, separated by `;`. The password column holds the BCrypt hash of the password, never the plaintext.

#### Scenario: File content after creation
- **WHEN** user Ana is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`

### Requirement: File reflects every change
After every successful create, delete or update, the users file SHALL reflect the current set of users and their current state. Rejected operations SHALL NOT modify the file.

#### Scenario: Update persisted
- **WHEN** a user's phone is updated
- **THEN** the corresponding line of the users file contains the new phone

### Requirement: Users restored on startup
On startup the application SHALL load all users stored in the users file, so previously stored users are available, including for uniqueness checks. A missing file means there are no users yet. Lines that do not contain exactly seven fields SHALL be ignored.

#### Scenario: Restart keeps users
- **WHEN** a user is created, the application is restarted, and `USER CREATE` is processed again with the same DNI
- **THEN** the response is `ERROR DUPLICATE DNI`

#### Scenario: No file yet
- **WHEN** the application starts and the users file does not exist
- **THEN** it starts with no users and without error
