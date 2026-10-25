# user-persistence Specification

## Purpose

Keeps users across application runs by storing them in a semicolon-separated text file and reloading them at startup.

## Requirements

### Requirement: Users stored in a text file
Users SHALL be stored persistently in a text file, one user per line, with the attributes first name, surname(s), DNI, email, password hash, telephone and card number in that order separated by `;`. No database or external persistence library SHALL be used.

#### Scenario: File content after creation
- **WHEN** user Ana is created with the example data
- **THEN** the users file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash and `;612345678;1234567890123456`

### Requirement: File reflects every change
After every successful create, delete or update operation the users file SHALL reflect the current set of users and their state.

#### Scenario: Deletion removes line
- **WHEN** an existing user is deleted successfully
- **THEN** the users file no longer contains that user's line

### Requirement: Users restored at startup
When the application starts, previously stored users SHALL be loaded from the users file so that they can be used by commands. A missing file SHALL be treated as an empty set of users.

#### Scenario: Restart keeps users
- **WHEN** a user is created, the application exits and is started again
- **THEN** the user exists, so creating another user with the same DNI is rejected and deleting with its credentials succeeds

#### Scenario: First start without file
- **WHEN** the application starts and no users file exists
- **THEN** it starts normally with no users
