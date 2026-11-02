# user-persistence Specification

## Purpose

Keeps users between runs of the application in a plain text file, using a dedicated persistence component separate from the user model and the main class.

## Requirements

### Requirement: Text file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card, for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`; from Delivery 3 the password column SHALL hold the BCrypt hash of the password instead of the password itself. (F1-C04, F3-C05)

#### Scenario: Stored line format
- **WHEN** the user Ana is created successfully
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<bcrypt-hash>;612345678;1234567890123456` whose fifth field is a BCrypt hash of `Clave1!`

### Requirement: Load on start-up and save on change
Previously stored users SHALL be loaded when the application starts, and every successful create, delete or update SHALL be reflected in the users file. (F1-C04)

#### Scenario: User survives restart
- **WHEN** a user is created, the application exits and is started again
- **THEN** the user is reconstructed with all seven attributes and still counts for uniqueness checks

#### Scenario: Failed save leaves state consistent
- **WHEN** writing the users file fails during a mutation
- **THEN** an error is reported and the in-memory users remain as they were before the command

### Requirement: Separated persistence without external technology
File reading and writing SHALL be implemented in a component separate from both the user model and the main class, without databases, persistence frameworks or external persistence libraries. (F1-C04)

#### Scenario: Architecture inspection
- **WHEN** the code is inspected
- **THEN** neither the user class nor the main class reads or writes the users file, and only the Java standard library is used
