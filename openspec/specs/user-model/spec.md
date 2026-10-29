# user-model Specification

## Purpose

Defines the park user as an independent, encapsulated domain object that keeps the data needed by user operations and protects its DNI from change.

## Requirements

### Requirement: User attributes
Each user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number, all accessible only through the user's public operations (encapsulated, not public fields).

#### Scenario: User retains its data
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** querying the user returns exactly those seven values

### Requirement: DNI is immutable
A user's DNI SHALL NOT be changeable after the user has been created.

#### Scenario: No way to change the DNI
- **WHEN** a user exists with DNI `12345678Z`
- **THEN** the user offers no operation to modify its DNI and the DNI remains `12345678Z` after any other attribute is modified

### Requirement: Separation of responsibilities
The user representation, the application start-up, and the storage/retrieval of users SHALL be in separate classes. The user class SHALL NOT perform persistence, and the main class SHALL NOT contain file-reading or file-writing logic.

#### Scenario: Responsibilities live in separate classes
- **WHEN** the source code is inspected
- **THEN** there is a user class without file I/O, a main class without file I/O for users, and a distinct class that stores and retrieves users
