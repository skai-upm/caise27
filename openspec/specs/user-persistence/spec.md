# user-persistence Specification

## Purpose

Defines how users survive between application runs using a plain text file, without databases, persistence frameworks or external libraries.

## Requirements

### Requirement: Users file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, telephone, card number. The password field SHALL contain only the BCrypt hash, never the original password.

#### Scenario: File line format
- **WHEN** the user Ana Garcia Lopez is created with password `Clave1!`
- **THEN** the users file contains a line beginning `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$`, ending `;612345678;1234567890123456`, and not containing `Clave1!`

### Requirement: Persistence across runs
Every successful create, delete and update SHALL be written to the users file, and previously stored users SHALL be loaded when the application starts.

#### Scenario: Users reloaded
- **WHEN** a user is created, the application exits, and the application is started again
- **THEN** the user exists and a `USER CREATE` with the same DNI or email is rejected

#### Scenario: No file yet
- **WHEN** the application starts and the users file does not exist
- **THEN** the application starts with no users and without error

### Requirement: Dedicated persistence responsibility
File reading and writing SHALL be implemented in a component separate from the user model and from the application entry point.

#### Scenario: Separation verified
- **WHEN** the source code is inspected
- **THEN** the user class and the main class contain no file reading or writing logic
