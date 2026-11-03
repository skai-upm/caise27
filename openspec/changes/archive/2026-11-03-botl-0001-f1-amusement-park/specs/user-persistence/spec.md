# Spec Delta

## Purpose

Preserves users between application runs in a plain text file, one user per line with `;`-separated attributes.

## ADDED Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file, one user per line, as `FirstName;Surnames;DNI;Email;Password;Phone;Card`.

#### Scenario: Stored line format
- **WHEN** Ana is created with the source example data
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Load on start-up
Previously stored users SHALL be loaded when the application starts, so users survive between runs.

#### Scenario: Restart keeps users
- **WHEN** a user is created, the application is stopped and started again
- **THEN** that user exists with all of its attributes

#### Scenario: Missing storage file
- **WHEN** the application starts and no storage file exists
- **THEN** it starts with no users and without error

#### Scenario: Unparseable stored line
- **WHEN** the storage file contains a line that does not describe a valid user
- **THEN** the line is skipped with a warning and the application starts normally

### Requirement: Persistence restrictions
User storage SHALL be implemented in a class separate from the user and main classes, without databases, persistence frameworks or external persistence libraries.

#### Scenario: Architecture inspection
- **WHEN** the code and build configuration are inspected
- **THEN** only a dedicated persistence class reads or writes the user file and no database or persistence library is used

### Requirement: No partial state on storage failure
If saving users fails, the operation SHALL be reported as an error and the in-memory users SHALL remain as they were before the operation.

#### Scenario: Save failure
- **WHEN** the storage file cannot be written during a user operation
- **THEN** an error is reported and the in-memory users are unchanged
