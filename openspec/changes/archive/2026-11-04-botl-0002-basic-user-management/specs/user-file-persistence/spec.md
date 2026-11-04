# Spec Delta

## Purpose

Defines how users are stored in a plain text file and recovered when the application starts again.

## ADDED Requirements

### Requirement: Text file format
Users SHALL be stored in a UTF-8 text file with one user per line and the attributes in the order first name, surname(s), DNI, email, password, phone, card number, separated by `;`. No database or external persistence library SHALL be used.

#### Scenario: File content after a creation
- **WHEN** the user from `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is created
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: File reflects every successful change
After each successful creation, deletion or update the file SHALL contain exactly the current set of users with their current values. A rejected command SHALL NOT change the file.

#### Scenario: Rejected command leaves the file unchanged
- **WHEN** a command is rejected for any reason
- **THEN** the file content is identical to its content before the command

### Requirement: Restart recovery
On startup the application SHALL load all users from the file, so that users created, updated or deleted in a previous run are recovered with their latest values. A missing file SHALL be treated as no users.

#### Scenario: Recovery after restart
- **WHEN** users are created, one is updated and one is deleted, and the application is restarted with the same file
- **THEN** exactly the remaining users are loaded, with their updated values, and duplicate checks apply to them

#### Scenario: First run without a file
- **WHEN** the application starts and the file does not exist
- **THEN** it starts with no users and creates the file on the first successful change
