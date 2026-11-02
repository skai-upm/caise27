# Spec Delta

## Purpose

Preserves users between runs of the application in a plain text file, one user per line, using a persistence component separate from the user model and the main class.

## ADDED Requirements

### Requirement: Text file user storage format
Users SHALL be stored in a text file, one user per line, with attributes separated by `;` in the order first name, surnames, DNI, email, password, telephone, card number (for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`).

#### Scenario: Stored line format
- **WHEN** the user Ana is created and storage is written
- **THEN** the storage file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Users survive restarts
Previously stored users SHALL be loaded when the application starts, and every successful user creation, deletion or modification SHALL be reflected in the storage file.

#### Scenario: Reload after restart
- **WHEN** a user is created, the application exits and is started again
- **THEN** the user is available with the same seven attribute values

### Requirement: Separated persistence responsibility
File reading and writing of users SHALL be implemented in a component separate from the user representation and from the main application class, without databases, persistence frameworks or external persistence libraries.

#### Scenario: Architecture inspection
- **WHEN** the code is inspected
- **THEN** neither the user representation nor the main class contains user file reading or writing logic, and only the Java standard library is used for persistence

### Requirement: Failed storage writes leave state unchanged
When a user mutation cannot be written to storage, the system SHALL report an error and keep the in-memory users identical to the stored users.

#### Scenario: Write failure
- **WHEN** a valid `USER CREATE` is processed but the storage file cannot be written
- **THEN** an error is reported and the user is not registered
