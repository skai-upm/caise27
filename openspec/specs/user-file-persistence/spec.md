# user-file-persistence Specification

## Purpose
Defines how users are stored in a plain semicolon-separated text file and recovered when the application starts again.

## Requirements

### Requirement: Text file format
The system SHALL store users in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number.

#### Scenario: File contents after creation
- **WHEN** user Ana is created with the appendix example data
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Persist after successful changes
After each successful create, delete or update, the file SHALL reflect the complete current set of users; failed commands SHALL NOT change the file.

#### Scenario: Rejected command leaves file untouched
- **GIVEN** a file with one user
- **WHEN** a duplicate creation is rejected
- **THEN** the file content is unchanged

### Requirement: Recovery at startup
At startup the system SHALL load all users from the file, so that the users and values current at the end of the previous run are available. A missing file SHALL be treated as no users.

#### Scenario: Restart after create, update and delete
- **GIVEN** a previous run created Ana and Pedro, updated Ana's first name, and deleted Pedro
- **WHEN** the application starts again with the same file
- **THEN** only Ana exists, with the updated first name, and Pedro's email can be used for a new user

#### Scenario: First start without a file
- **GIVEN** no users file exists
- **WHEN** the application starts
- **THEN** it starts with no users and without error
