# Spec Delta

## Purpose

Defines the user as an independent domain object of the amusement park system: the data each user holds, its encapsulation, and the immutability of the DNI.

## ADDED Requirements

### Requirement: User data
The system SHALL represent each user as an independent object holding first name, surname(s), DNI, email address, password, telephone number and card number, and SHALL return each value exactly as it was set.

#### Scenario: Create a user with all attributes
- **WHEN** a user is created with first name, surname(s), DNI, email address, password, telephone number and card number
- **THEN** each of the seven values can be retrieved from the user unchanged

### Requirement: Attribute encapsulation
User attributes SHALL only be read or changed through the user's own operations; no attribute SHALL be directly accessible from outside the user.

#### Scenario: Update a mutable attribute
- **WHEN** the email address (or any attribute other than the DNI) of an existing user is changed through the user's operations
- **THEN** the user returns the new value and all other attributes remain unchanged

### Requirement: Immutable DNI
A user's DNI SHALL NOT be changeable after the user has been created.

#### Scenario: No way to change the DNI
- **WHEN** a user has been created with a given DNI
- **THEN** the user offers no operation that changes the DNI and the DNI returned is always the one given at creation

### Requirement: User independent of persistence and startup
The user representation SHALL NOT read or write files nor start or run the application; those responsibilities SHALL belong to separate classes.

#### Scenario: User has no persistence responsibility
- **WHEN** the user class is inspected
- **THEN** it contains no file-reading, file-writing or application-startup logic
