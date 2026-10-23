# user-persistence Specification

## Purpose

Defines how park users survive between application runs using a plain text file, without databases, persistence frameworks or external libraries.

## Requirements

### Requirement: User file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password hash, phone, card, where the password hash is a BCrypt value and the original password never appears in the file.

#### Scenario: Stored line
- **WHEN** the user Ana Garcia Lopez is created with the example data
- **THEN** the users file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending `;612345678;1234567890123456`, and does not contain `Clave1!`

#### Scenario: Password change stores a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored hash changes, `Nueva2#` does not appear in the file, and the new password authenticates after a restart

### Requirement: Load at startup
Previously stored users SHALL be loaded when the application starts; if the file does not exist the application SHALL start with no users, and malformed lines SHALL be skipped with a warning rather than aborting startup.

#### Scenario: Users survive a restart
- **WHEN** a user is created in one run and the application is started again
- **THEN** the user exists in the new run (e.g. creating it again is rejected as a duplicate)

#### Scenario: Missing users file
- **WHEN** the application starts and no users file exists
- **THEN** it starts normally with no users

### Requirement: Persist every change
Every successful creation, deletion or modification SHALL be written to the users file before its success message is printed.

#### Scenario: Deletion persisted
- **WHEN** a user is deleted and the application is restarted
- **THEN** the deleted user is not loaded
