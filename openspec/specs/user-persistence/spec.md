# user-persistence Specification

## Purpose

Defines how users are preserved between runs in a plain text file, without databases, persistence frameworks or external persistence libraries (F1-C04).

## Requirements

### Requirement: Text file format
Users SHALL be stored in a text file, one user per line, with attributes in the order first name, surnames, DNI, email, password, telephone, card, separated by `;` (for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`). From Delivery 3 the password attribute SHALL hold the BCrypt hash of the password instead of the password itself.

#### Scenario: Created user written as one line
- **WHEN** a user is successfully created
- **THEN** the users file contains one line for that user with its seven attributes separated by `;` in the defined order

#### Scenario: Password column holds a hash
- **WHEN** a user is created from Delivery 3 onward
- **THEN** the fifth attribute of its line is a BCrypt hash and the plaintext password does not appear in the file

### Requirement: Load at start-up
Previously stored users SHALL be loaded when the application starts, so that they exist again with the same state in the new run.

#### Scenario: User survives restart
- **WHEN** a user is created, the application exits, and the application is started again
- **THEN** the user exists with the same attributes and its DNI and email remain reserved

#### Scenario: No file yet
- **WHEN** the application starts and no users file exists
- **THEN** it starts with no users and without error

### Requirement: Persistence after every successful change
Every successful creation, deletion or update SHALL be reflected in the users file. A rejected operation SHALL leave the users file unchanged.

#### Scenario: Rejected operation leaves file untouched
- **WHEN** a `USER CREATE` with a duplicate email is rejected
- **THEN** the users file content is unchanged

### Requirement: Persistence isolated in its own class
Reading and writing the users file SHALL be implemented in a class separate from the user class and the main class, using only the Java standard library.

#### Scenario: Architecture inspection
- **WHEN** the source code is inspected
- **THEN** file reading and writing of users is found only in the persistence class, and no database, persistence framework or external library is used
