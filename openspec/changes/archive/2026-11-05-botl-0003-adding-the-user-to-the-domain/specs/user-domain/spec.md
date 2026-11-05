# Spec Delta

## Purpose

Defines the user of the amusement park as an independent domain object: the data it holds, its encapsulation, and the immutability of its DNI.

## ADDED Requirements

### Requirement: User attributes
The system SHALL represent each user as an independent object holding exactly these data: first name, surname(s), DNI, email address, password, telephone number and card number. Each value supplied when the user is created SHALL be retrievable from the user unchanged.

#### Scenario: User created with all attributes
- **GIVEN** first name "Ana", surnames "García López", DNI "12345678Z", email "ana@example.com", password "Secret12", telephone "600123456" and card number "4111111111111111"
- **WHEN** a user is created with these data
- **THEN** the user returns each of these values for the corresponding attribute

#### Scenario: Users are independent objects
- **GIVEN** two users created with different data
- **WHEN** an attribute of one user is changed
- **THEN** the other user's attributes remain unchanged

### Requirement: Encapsulated user state
The user's attributes SHALL be encapsulated: they MUST NOT be directly accessible or modifiable from outside the user object other than through the operations the user exposes.

#### Scenario: Attributes only reachable through the user's operations
- **WHEN** the user type is inspected
- **THEN** none of its attribute fields is publicly accessible

#### Scenario: Mutable attribute changed through the user
- **GIVEN** an existing user with email "ana@example.com"
- **WHEN** the email is changed to "ana.garcia@example.com" through the user's operation
- **THEN** the user returns "ana.garcia@example.com" as its email

### Requirement: Immutable DNI
A user's DNI SHALL be set when the user is created and MUST NOT be changeable afterwards.

#### Scenario: DNI cannot be changed after creation
- **GIVEN** an existing user with DNI "12345678Z"
- **WHEN** an attempt is made to change its DNI
- **THEN** no operation of the user allows it and the user still has DNI "12345678Z"

#### Scenario: Other changes keep the DNI
- **GIVEN** an existing user with DNI "12345678Z"
- **WHEN** its first name, surnames, email, password, telephone and card number are changed
- **THEN** the user still has DNI "12345678Z"
