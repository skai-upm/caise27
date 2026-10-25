# user-persistence Specification

## Purpose

Defines how users are stored in a plain text file between executions and restored when the application starts, without databases or external persistence libraries.

## Requirements

### Requirement: Semicolon-separated user file
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order: first name, surname(s), DNI, email, password hash, telephone, card number. The password field SHALL contain a BCrypt hash, never the plaintext password.

#### Scenario: File content after creation
- **WHEN** user Ana is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2` followed by the rest of the BCrypt hash and `;612345678;1234567890123456`, and does not contain `Clave1!`

### Requirement: Persist after every change
The users file SHALL be rewritten after every successful create, delete or update so that it always reflects the current set of users.

#### Scenario: Deletion reflected in file
- **WHEN** a stored user is deleted with `USER DELETE`
- **THEN** the users file no longer contains that user's line

#### Scenario: Update reflected in file
- **WHEN** a stored user's phone is updated with `USER UPDATE`
- **THEN** the users file contains the user's line with the new phone

### Requirement: Restore users at startup
When the application starts, it SHALL load all users from the users file, so previously stored users are available to every command. If the file does not exist yet, the application SHALL start with no users.

#### Scenario: Users survive restart
- **WHEN** a user is created, the application exits, and the application is started again
- **THEN** the user exists and can be deleted or updated with its stored credentials

#### Scenario: First execution without file
- **WHEN** the application starts and no users file exists
- **THEN** it starts with no users and without errors

### Requirement: Malformed stored lines are tolerated
When loading, lines that are blank or do not contain exactly seven fields SHALL be skipped with a warning instead of terminating the application.

#### Scenario: Corrupted line
- **WHEN** the users file contains a line with fewer than seven fields
- **THEN** that line is skipped, a warning is printed, and the remaining users are loaded

### Requirement: Legacy plaintext migration
When loading, a stored password that is not a BCrypt hash SHALL be treated as a legacy plaintext password, replaced by its hash, and the users file SHALL be rewritten.

#### Scenario: Delivery 2 users file
- **WHEN** the users file contains a user with plaintext password `Clave1!` and the application starts
- **THEN** the user can authenticate with `Clave1!` and the file now stores a hash instead of the plaintext
