# Spec Delta

## ADDED Requirements

### Requirement: Loaded users satisfy creation rules
When loading users at start-up, the system SHALL load a stored line only if its DNI, email, password and card number satisfy the user-validation rules and its DNI and email are not already used by a previously loaded user; any other line SHALL be ignored, and the application SHALL print a warning stating how many stored lines were ignored.

#### Scenario: Invalid stored user ignored
- **WHEN** the users file contains a valid line for DNI `12345678Z` and a seven-field line whose DNI is `12345678A`
- **THEN** only the user `12345678Z` is loaded
- **AND** a warning reporting 1 ignored line is printed before any command response

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
