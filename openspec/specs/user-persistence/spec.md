# user-persistence Specification

## Purpose

Keeps users between application runs by storing them in a plain text file and loading them at start-up, without databases or persistence frameworks.

## Requirements

### Requirement: Text file format
Users SHALL be stored in the text file `users.txt` in the data directory (the working directory in normal mode, the temporary directory in `--fly` mode), one user per line, with attributes in the order first name, surname(s), DNI, email, BCrypt password hash, phone, card, separated by `;`.

#### Scenario: Stored line format
- **WHEN** user Ana (`Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is created
- **THEN** the file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending `;612345678;1234567890123456`, and does not contain `Clave1!`

### Requirement: Changes are persisted
Every successful create, delete or update SHALL be reflected in the persistence file before the next command is processed.

#### Scenario: Users survive a restart
- **WHEN** a user is created, the application exits and is started again
- **THEN** the user exists and operations on it succeed

### Requirement: Users loaded at start-up
On start-up the application SHALL load all previously stored users. A missing file SHALL mean no users. A line that cannot be parsed into a user SHALL be skipped with a warning without stopping the application.

#### Scenario: Missing file
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** the application starts with no users and no error terminates it

#### Scenario: Malformed line
- **WHEN** `users.txt` contains a line with fewer than seven fields
- **THEN** that line is ignored, a warning is shown, and the other users are loaded

### Requirement: Persistence isolated in its own class
File reading and writing SHALL be implemented in a class separate from the user class and the main class, using only the Java standard library.

#### Scenario: Persistence class is separate
- **WHEN** the source code is inspected
- **THEN** only the persistence class reads or writes `users.txt`
