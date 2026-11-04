# user-model Specification

## Purpose

Defines the information the system holds about each park user and the guarantees on that data, such as the DNI never changing after creation.

## Requirements

### Requirement: User data fields
The system SHALL hold, for every user, a first name, surname(s), DNI, email address, password hash, telephone number and card number, exposed only through the user's own accessors (encapsulated state).

#### Scenario: Created user exposes all fields
- **GIVEN** a valid `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` command
- **WHEN** it is processed
- **THEN** the stored user has first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, a password hash that verifies `Clave1!`, phone `612345678` and card `1234567890123456`

### Requirement: Immutable DNI
The DNI of a user SHALL NOT change after the user has been created.

#### Scenario: DNI cannot be updated
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the command is rejected as a non-editable field and the stored DNI remains `12345678Z`

### Requirement: Separation of user representation and storage
The user representation SHALL NOT read or write the persistence file, and the application's main class SHALL NOT implement file reading or writing; a dedicated persistence component SHALL store and return user objects.

#### Scenario: Responsibility review
- **WHEN** the production code is reviewed
- **THEN** file input/output for users exists only in the persistence component, which receives and returns user objects
