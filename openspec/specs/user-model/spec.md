# user-model Specification

## Purpose
Defines the data held for each park user and the identity guarantees of that data, independent of how users are validated or stored.

## Requirements

### Requirement: User data
The system SHALL hold, for each user, a first name, surname(s), DNI, email address, password, telephone number and card number, and SHALL expose them only through the user's own accessors.

#### Scenario: All fields retained
- **GIVEN** a user created with `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **WHEN** the user's data is read back
- **THEN** each of the seven values equals the value supplied at creation

### Requirement: Immutable DNI
The DNI of a user SHALL NOT change after the user has been created, and no command SHALL offer a way to change it.

#### Scenario: DNI not editable via update
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the command is rejected as an unsupported field and the user's DNI remains `12345678Z`

### Requirement: User independent of storage
The user representation SHALL NOT read or write the persistence file; storage SHALL be performed by a separate component, and the application's main class SHALL NOT implement file reading or writing.

#### Scenario: Responsibility separation
- **WHEN** the production code is reviewed
- **THEN** file input/output for users exists only in the dedicated storage class, not in the user class or the main class
