# Spec Delta

## Purpose

Defines the user as an independent, encapsulated domain object of the Amusement Park system, holding the data later user-management features rely on, with an immutable DNI.

## ADDED Requirements

### Requirement: User attributes
The system SHALL represent each user as an independent object holding exactly these data: first name, surname(s), DNI, email address, password, telephone number and card number. Each value supplied at creation MUST be retrievable unchanged from the created user.

#### Scenario: Create a user with all attributes
- **WHEN** a user is created with first name `Ana`, surnames `García López`, DNI `12345678Z`, email `ana@example.com`, password `secreto1`, telephone `600123456` and card number `4111111111111111`
- **THEN** reading each attribute of the user returns exactly the supplied value

#### Scenario: Users are independent objects
- **WHEN** two users are created with different data
- **THEN** changing a mutable attribute of one user leaves every attribute of the other user unchanged

### Requirement: Attribute encapsulation
User attributes MUST NOT be directly accessible from outside the user object; they SHALL be read and, where allowed, changed only through the user's operations.

#### Scenario: No publicly exposed fields
- **WHEN** the user type is inspected
- **THEN** it exposes no public or package-visible fields for its attributes

#### Scenario: Mutable attribute changed through its operation
- **WHEN** the email of an existing user is changed to `nuevo@example.com` through the user's operation
- **THEN** reading the email returns `nuevo@example.com`
- **AND** the user's other attributes are unchanged

### Requirement: Immutable DNI
A user's DNI SHALL be fixed at creation and MUST NOT be changeable afterwards.

#### Scenario: DNI cannot be modified
- **WHEN** an existing user with DNI `12345678Z` is inspected for any operation that changes its DNI
- **THEN** no such operation exists
- **AND** the DNI read from the user remains `12345678Z` after its other attributes are modified

### Requirement: User object free of persistence
The user object SHALL only represent user data; it MUST NOT read or write files or otherwise manage persistence, and it SHALL be separate from the class that starts the application.

#### Scenario: User performs no I/O
- **WHEN** users are created, read and modified
- **THEN** no file is created, read or written by the user object
