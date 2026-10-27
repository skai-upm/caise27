# Spec Delta

## Purpose

Defines the users of the amusement park application: the data each user holds, the invariants on that data, and how users are stored and retrieved separately from the user representation and the application entry point.

## ADDED Requirements

### Requirement: User data
Each user SHALL hold the following data: first name, surname(s), DNI, email address, password, telephone number and card number. The data SHALL only be accessible through the user's operations (encapsulated attributes).

#### Scenario: User keeps its data
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** the user returns exactly those values for each attribute

### Requirement: DNI is immutable
A user's DNI SHALL NOT be changeable after the user is created.

#### Scenario: No DNI modification
- **WHEN** a user has been created with DNI `12345678Z`
- **THEN** the user offers no operation to change the DNI and keeps returning `12345678Z`

### Requirement: Separate user storage
Users SHALL be stored and retrieved by a component separate from the user representation and from the application entry point; the user itself SHALL NOT manage storage.

#### Scenario: Store and retrieve a user
- **WHEN** a user with email `ana@correo.com` is stored
- **THEN** it can be retrieved by that email address

#### Scenario: Unknown user
- **WHEN** a user is looked up by an email address that was never stored
- **THEN** no user is returned
