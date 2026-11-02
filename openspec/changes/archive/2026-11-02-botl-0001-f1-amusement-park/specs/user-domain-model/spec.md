# Spec Delta

## Purpose

Defines the user as an independent, encapsulated domain object holding the data required by the user-management operations (F1-C01).

## ADDED Requirements

### Requirement: User attributes
A user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number, all encapsulated and readable through the user's public interface.

#### Scenario: Attributes available after creation
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456`
- **THEN** each of those seven values can be read back unchanged from the user

### Requirement: Immutable DNI
A user's DNI SHALL NOT be modifiable after the user is created; no operation of the system SHALL change it.

#### Scenario: DNI cannot be changed
- **WHEN** any operation attempts to modify the DNI of an existing user
- **THEN** the attempt is rejected and the user keeps the DNI it was created with

### Requirement: Separation of responsibilities
The system SHALL keep separate units for representing a user, starting and running the application, and storing and retrieving users. The user representation SHALL NOT manage persistence, and the application entry point SHALL NOT contain file-reading or file-writing logic for users.

#### Scenario: Architecture inspection
- **WHEN** the source code is inspected
- **THEN** user representation, application start-up and user storage are implemented in different classes, and neither the user class nor the main class reads or writes the users file
