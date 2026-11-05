# user-persistence Specification

## Purpose
Preserves users between runs of the Amusement Park application by storing them in a `;`-separated text file that is loaded at start-up, with file access isolated from the user and main classes.

## Requirements

### Requirement: Users file format
Users SHALL be stored in a text file named `users.txt` in the application's working directory, one user per line, with the attributes in the order first name, surnames, DNI, email, protected password (BCrypt value), telephone, card number, separated by `;`, for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$10$<53 BCrypt characters>;612345678;1234567890123456`. The original password SHALL never be written to the file.

#### Scenario: Created user written as one line
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** `users.txt` contains one line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending with `;612345678;1234567890123456`
- **AND** the line does not contain `Clave1!`

#### Scenario: Several users in creation order
- **WHEN** users with DNI `12345678Z` and then `87654321X` are created
- **THEN** `users.txt` contains exactly two lines, the first for `12345678Z` and the second for `87654321X`

### Requirement: Users saved after each change
After every successful change to the set of users, the system SHALL write the complete current set of users to the users file, so that no explicit exit command is needed for the data to be preserved. A rejected command MUST NOT change the file.

#### Scenario: Rejected creation leaves file unchanged
- **WHEN** a `USER CREATE` with a duplicate DNI is rejected
- **THEN** the content of `users.txt` is the same as before the command

### Requirement: Users loaded at start-up
When the application starts, it SHALL load all users from the users file before processing any command, mapping each field to the corresponding user attribute. If the file does not exist, the application SHALL start with no users and without error.

#### Scenario: Users recovered after restart
- **WHEN** a user with DNI `12345678Z` is created, the application exits, and the application is started again
- **THEN** the user with DNI `12345678Z` exists with the same first name, surnames, email, password, telephone and card number

#### Scenario: Uniqueness applies to loaded users
- **WHEN** after a restart `USER CREATE` is processed with DNI `12345678Z` already present in the file
- **THEN** the command is rejected as a duplicate DNI

#### Scenario: Missing file
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** it starts with no users and processes commands normally

#### Scenario: Malformed line ignored
- **WHEN** the users file contains a line that does not have exactly seven `;`-separated fields
- **THEN** that line is ignored, the remaining valid lines are loaded and the application keeps running

### Requirement: Separated persistence responsibility
Reading and writing the users file SHALL be done by a dedicated class distinct from the user class and the main class, using only the Java standard library; databases, persistence frameworks and external persistence libraries MUST NOT be used.

#### Scenario: Persistence isolated
- **WHEN** the source code is inspected
- **THEN** file reading/writing of users is located only in the persistence class
- **AND** the user class and the main class contain no file reading or writing logic

### Requirement: Loaded users satisfy creation rules
When loading users at start-up, the system SHALL load a stored line only if its DNI, email and card number satisfy the user-validation rules, its password field is a BCrypt-protected value, and its DNI and email are not already used by a previously loaded user; any other line (including lines with plain-text passwords written by earlier versions, which are not migrated) SHALL be ignored, and the application SHALL print a warning stating how many stored lines were ignored.

#### Scenario: Invalid stored user ignored
- **WHEN** the users file contains a valid line for DNI `12345678Z` and a seven-field line whose DNI is `12345678A`
- **THEN** only the user `12345678Z` is loaded
- **AND** a warning reporting 1 ignored line is printed before any command response

#### Scenario: Plain-text password line ignored
- **WHEN** the users file contains a line whose password field is `Clave1!`
- **THEN** that user is not loaded and the line is reported as ignored

#### Scenario: Duplicate stored DNI ignored
- **WHEN** the users file contains two valid lines with DNI `12345678Z`
- **THEN** only the first one is loaded and 1 ignored line is reported

#### Scenario: Clean file produces no warning
- **WHEN** every line of the users file is valid and unique
- **THEN** all users are loaded and no warning is printed

### Requirement: Unreadable users file handled at start-up
If the users file exists but cannot be read, the application SHALL print an error message starting with `ERROR` that names the users file, MUST NOT terminate with an uncaught exception, and MUST NOT process any command, so the unreadable data is never overwritten.

#### Scenario: Users file is a directory
- **WHEN** the application starts and `users.txt` is a directory
- **THEN** an `ERROR` message mentioning `users.txt` is printed, no stack trace is printed, and no command is executed
