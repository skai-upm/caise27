# user-persistence Specification

## Purpose

Keeps registered users across application runs using a plain semicolon-separated text file, without databases or persistence libraries (source F1-C04, section 7).

## Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, telephone, card number, where from Delivery 3 the password field holds the BCrypt hash of the password instead of the password itself (e.g. `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$10$...;612345678;1234567890123456`).

#### Scenario: Created user written as one line
- **WHEN** a user is created successfully
- **THEN** the user data file contains a line with that user's seven attributes separated by `;` in the specified order

#### Scenario: Password field holds a hash
- **WHEN** a user created with password `Clave1!` is stored
- **THEN** the fifth field of the line is a BCrypt hash and not `Clave1!`

### Requirement: Users loaded at start-up
Previously stored users SHALL be loaded when the application starts, so that users created in an earlier run exist with the same attribute values.

#### Scenario: User survives restart
- **WHEN** a user is created, the application exits, and the application is started again
- **THEN** the user exists with all seven attributes reconstructed and still counts for uniqueness checks

#### Scenario: Missing data file on first run
- **WHEN** the application starts and no user data file exists yet
- **THEN** the application starts with no users and without error

### Requirement: Persistence isolated from user model and main class
File reading and writing of user data SHALL be implemented in a class separate from the user class and the main class, without databases, persistence frameworks or external persistence libraries.

#### Scenario: Architecture keeps persistence separate
- **WHEN** the source code is inspected
- **THEN** only the dedicated persistence class reads or writes the user data file and no database or persistence library is used
