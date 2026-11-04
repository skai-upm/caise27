# Spec Delta

## Purpose

Defines the data held for each user of the amusement-park application and which parts of it may change after creation.

## ADDED Requirements

### Requirement: User data
Each user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number. These attributes SHALL only be accessible through the user's own operations (encapsulation).

#### Scenario: User created with all fields
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** each of the seven values can be read back unchanged from the user

### Requirement: Immutable DNI
A user's DNI SHALL NOT change after the user has been created.

#### Scenario: Attempt to change the DNI through an update
- **WHEN** an authenticated `USER UPDATE` names the field `DNI`
- **THEN** the update is rejected and the user's DNI keeps its original value

### Requirement: Separation of user representation and storage
The user representation SHALL NOT read or write the persistence file, and the application entry point SHALL NOT implement reading or writing that file; a separate component SHALL store and load users.

#### Scenario: Code structure review
- **WHEN** the production sources are reviewed
- **THEN** file reading and writing of users is found only in the dedicated persistence component, not in the user class or the main class
