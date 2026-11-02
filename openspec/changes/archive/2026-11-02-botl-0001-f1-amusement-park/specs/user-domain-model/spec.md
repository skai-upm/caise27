# Spec Delta

## Purpose

Represents users as independent, encapsulated domain objects holding the data required by user management operations, with an immutable DNI.

## ADDED Requirements

### Requirement: User attributes
The system SHALL represent each user as an independent object that holds first name, surname(s), DNI, email address, password, telephone number and card number. Attributes SHALL be encapsulated: they are accessible only through the user's own operations, not as publicly mutable state.

#### Scenario: Newly created user exposes its data
- **WHEN** a user is created with first name, surnames, DNI, email, password, telephone and card number
- **THEN** each of those seven values can be read back unchanged through the user's operations

### Requirement: Immutable DNI
A user's DNI SHALL NOT change after the user is created.

#### Scenario: DNI modification is impossible
- **WHEN** any operation attempts to change the DNI of an existing user
- **THEN** the change is prohibited and the user's DNI keeps its original value

### Requirement: User is not responsible for persistence
The user representation SHALL NOT read or write storage; persistence SHALL be handled by a separate component.

#### Scenario: User model has no file access
- **WHEN** the user representation is inspected
- **THEN** it contains no file reading or writing logic
