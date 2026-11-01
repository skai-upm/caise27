# user-persistence Specification

## Purpose

Keeps registered users across application executions by storing them in a plain semicolon-separated text file and reconstructing them at startup.

## Requirements

### Requirement: Text-file user storage format
The system SHALL store users in a text file with one user per line and attributes separated by `;` in the order: first name, surname(s), DNI, email, protected password, telephone, card number. The protected password SHALL be the BCrypt hash of the password (or, for users written by an earlier delivery and not yet changed, the value originally stored). No database or external persistence library SHALL be used.

#### Scenario: Stored line format
- **WHEN** the user Ana Garcia Lopez with DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456` is stored
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`

### Requirement: Users are restored at startup
The system SHALL load every previously stored user from the users file when the application starts. A missing users file SHALL be treated as an empty user set.

#### Scenario: Restart restores users
- **GIVEN** a valid user was created during one execution
- **WHEN** the application terminates and starts again
- **THEN** the user is reconstructed from the text file with the saved attributes

#### Scenario: First start without a users file
- **GIVEN** no users file exists
- **WHEN** the application starts
- **THEN** it starts with no registered users and without an error

### Requirement: Changes are persisted immediately
After every successful user creation, deletion, or update the system SHALL rewrite the users file so that it reflects the current set of users. Rejected operations SHALL leave the users file unchanged.

#### Scenario: Persisted updates and deletions
- **GIVEN** a previously stored user
- **WHEN** a valid update or deletion is performed and the application restarts
- **THEN** the file-backed state reflects that operation

#### Scenario: Rejected operation leaves file unchanged
- **GIVEN** a stored users file
- **WHEN** a user creation, deletion, or update is rejected
- **THEN** the users file content is unchanged

### Requirement: Persistence is a separate responsibility
Reading and writing the users file SHALL be implemented by a component distinct from both the user representation and the application's main entry point, which receives and returns user objects.

#### Scenario: Responsibility separation
- **WHEN** the project structure is inspected
- **THEN** the user representation, the application entry/terminal interaction, and the file storage/loading are implemented in distinct classes, and neither the user class nor the main class reads or writes the users file
