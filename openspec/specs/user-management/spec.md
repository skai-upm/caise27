# user-management Specification

## Purpose

Defines the user concept of the Amusement Park application and the rules that govern user data, as the basis for all user management operations.

## Requirements

### Requirement: User attributes
The system SHALL represent each user as an independent object holding a first name, surname(s), DNI, email address, password, telephone number and card number, exactly as supplied at creation.

#### Scenario: User created with all attributes
- **GIVEN** a first name, surname(s), DNI, email address, password, telephone number and card number
- **WHEN** a user is created with that data
- **THEN** each attribute of the user returns the value supplied for it

#### Scenario: Users are independent objects
- **GIVEN** two users created with different data
- **WHEN** an attribute of one user is changed
- **THEN** the other user's attributes remain unchanged

### Requirement: Encapsulated user data
User attributes MUST be encapsulated: they SHALL be readable and, where allowed, changeable only through the user's own operations, never by direct field access from other components.

#### Scenario: Modifiable attribute changed through its operation
- **GIVEN** an existing user
- **WHEN** its email address is changed through the user's operation
- **THEN** reading the email address returns the new value and all other attributes are unchanged

### Requirement: Immutable DNI
A user's DNI SHALL NOT be changeable after the user has been created.

#### Scenario: DNI cannot be changed
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** any attempt is made to change its DNI
- **THEN** no operation for changing the DNI is available and the user's DNI remains `12345678Z`

### Requirement: User has no persistence responsibility
The user representation MUST NOT read or write files or otherwise manage its own persistence, and the application entry point MUST NOT contain file-reading or file-writing logic.

#### Scenario: Creating a user has no side effects on storage
- **GIVEN** an empty working directory
- **WHEN** a user is created
- **THEN** no file is created or modified
