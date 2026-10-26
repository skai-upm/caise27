# user-persistence Specification

## Purpose

Defines how users are stored in a plain text file and restored when the application starts, without databases or external libraries.

## Requirements

### Requirement: Semicolon-separated user file
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, phone, card number. Plain-text passwords SHALL NOT be written to the file.

#### Scenario: File content after creation
- **WHEN** user Ana (`Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is created
- **THEN** the file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash (starting with `$2a$`) and `;612345678;1234567890123456`, and does not contain `Clave1!`

### Requirement: File reflects every change
After every successful create, delete or update, the user file SHALL contain exactly the current set of users.

#### Scenario: Deletion persisted
- **WHEN** a user is deleted
- **THEN** that user's line is no longer present in the file

### Requirement: Users restored at startup
When the application starts it SHALL load all users from the file; if the file does not exist the application SHALL start with no users. Lines that cannot be parsed SHALL be skipped without stopping the application.

#### Scenario: Restart restores users
- **WHEN** a user was created, the application is closed and started again
- **THEN** the user exists and a create command reusing its DNI is rejected as duplicate

#### Scenario: Missing file
- **WHEN** the application starts and the user file does not exist
- **THEN** the application starts with no users and creates the file on the first change

### Requirement: Migration of plain-text passwords
When users are loaded, any stored password that is not a BCrypt hash SHALL be treated as a legacy plain-text password: it SHALL be replaced by its hash and the user file SHALL be rewritten without plain-text passwords.

#### Scenario: Legacy file
- **WHEN** the user file contains `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` and the application starts
- **THEN** Ana can still authenticate with `Clave1!` and the file no longer contains `Clave1!`
