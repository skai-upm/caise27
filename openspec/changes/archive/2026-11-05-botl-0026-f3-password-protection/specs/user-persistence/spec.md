## MODIFIED Requirements

### Requirement: Users file format
Users SHALL be stored in a text file named `users.txt` in the application's working directory, one user per line, with the attributes in the order first name, surnames, DNI, email, protected password (BCrypt value), telephone, card number, separated by `;`, for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$10$<53 BCrypt characters>;612345678;1234567890123456`. The original password SHALL never be written to the file.

#### Scenario: Created user written as one line
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** `users.txt` contains one line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending with `;612345678;1234567890123456`
- **AND** the line does not contain `Clave1!`

#### Scenario: Several users in creation order
- **WHEN** users with DNI `12345678Z` and then `87654321X` are created
- **THEN** `users.txt` contains exactly two lines, the first for `12345678Z` and the second for `87654321X`

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
