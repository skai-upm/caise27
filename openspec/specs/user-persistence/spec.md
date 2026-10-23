# user-persistence Specification

## Purpose

Keeps registered users between runs of the application by storing them in a plain text file that is loaded at start-up, without databases or persistence libraries.

## Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file (`users.txt` in the working directory by default) with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card.

#### Scenario: File contents after creation
- **WHEN** Ana and then Pedro are created successfully
- **THEN** the file contains `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` followed by `Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654`

### Requirement: Save after every change
Every successful create, update or delete SHALL be written to the storage file before the success message is printed. Failed operations SHALL NOT modify the file.

#### Scenario: Data survives a restart
- **WHEN** a user is created, the application exits and is started again
- **THEN** `USER UPDATE` and `USER DELETE` with that user's credentials succeed without re-creating it

### Requirement: Load at start-up
On start-up the application SHALL load all users from the storage file. A missing file SHALL be treated as an empty user list. Lines that are blank are ignored; lines that do not have exactly seven fields SHALL be skipped with a warning instead of aborting start-up.

#### Scenario: No storage file yet
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** it starts normally with no users

#### Scenario: Corrupt line
- **WHEN** the storage file contains one valid line and one line with three fields
- **THEN** the valid user is loaded, a warning is printed for the corrupt line, and the application keeps running
