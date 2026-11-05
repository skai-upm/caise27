# Spec Delta

## Purpose

Defines the user as an independent domain object of the amusement park: the data each user holds, how that data is encapsulated, and which parts of a user's identity can never change.

## ADDED Requirements

### Requirement: User attributes
Each user SHALL hold a first name, surname(s), DNI, e-mail address, password (its stored protected value), telephone number and card number. The attributes SHALL be encapsulated: other objects SHALL read them through the user's operations and SHALL NOT access its state directly. A user SHALL be created with all its attributes present.

#### Scenario: User created with its data
- **GIVEN** the data `Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, a protected password, `612345678` and `1234567890123456`
- **WHEN** a user is created with it
- **THEN** each attribute can be read back with the same value

#### Scenario: Missing attribute rejected
- **WHEN** a user is created without one of its attributes
- **THEN** creation is rejected and no user exists

### Requirement: Immutable DNI
A user's DNI SHALL NOT change after creation. The other attributes MAY be changed through the user's own operations, for later management features.

#### Scenario: DNI cannot be changed
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** an attempt is made to change the DNI
- **THEN** no operation allows it and the DNI remains `12345678Z`

#### Scenario: Other data can change
- **GIVEN** an existing user
- **WHEN** their phone is changed to `699999999`
- **THEN** the user's phone is `699999999` and the DNI is unchanged

### Requirement: Users do not manage persistence
The user class SHALL represent the user only. It SHALL NOT read or write files, and storing and retrieving users SHALL be the responsibility of a separate class. The application entry point SHALL NOT contain user file I/O.

#### Scenario: Separation checked
- **WHEN** the user class is inspected
- **THEN** it has no file or persistence dependency
