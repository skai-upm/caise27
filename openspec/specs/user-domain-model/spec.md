# user-domain-model Specification

## Purpose

Introduces the user as an independent, encapsulated domain object holding the data needed by user management, with an identity document (DNI) that never changes.

## Requirements

### Requirement: User attributes
A user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number, each accessible only through the user's own operations (encapsulated state). (F1-C01)

#### Scenario: Attributes available after creation
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** reading the user's state returns exactly those seven values

### Requirement: Immutable DNI
A user's DNI SHALL NOT be modifiable after the user has been created. (F1-C01)

#### Scenario: DNI cannot be changed
- **WHEN** any operation attempts to change the DNI of an existing user
- **THEN** the change is not possible and the user keeps the DNI it was created with
