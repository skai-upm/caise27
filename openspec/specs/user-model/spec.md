# user-model Specification

## Purpose

Defines the user as an independent domain object with encapsulated personal data, including the immutable DNI that identifies it.

## Requirements

### Requirement: User attributes
A user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number, all retrievable after creation.

#### Scenario: Create a user with all attributes
- **WHEN** a user is created with first name `Ana`, surnames `García López`, DNI `12345678Z`, email `ana@example.com`, password `secret1`, telephone `600123456` and card number `4111111111111111`
- **THEN** each attribute can be read back with exactly the value given at creation

### Requirement: Encapsulated user state
User attributes SHALL be encapsulated: their state is only accessible through the user's operations and not exposed as public fields.

#### Scenario: No public mutable fields
- **WHEN** the user type is inspected
- **THEN** it exposes no public fields

### Requirement: Immutable DNI
A user's DNI SHALL NOT be changeable after creation, while the remaining attributes MAY be updated.

#### Scenario: DNI cannot be modified
- **WHEN** a user has been created with DNI `12345678Z`
- **THEN** the user offers no operation that changes its DNI and the DNI remains `12345678Z`

#### Scenario: Other attributes can be updated
- **WHEN** the email of an existing user is updated to `new@example.com`
- **THEN** the user's email reads `new@example.com` and its DNI is unchanged

### Requirement: User independent of persistence
The user type SHALL NOT perform storage or file input/output; storing and retrieving users and starting the application SHALL be responsibilities of other types.

#### Scenario: User has no persistence dependency
- **WHEN** the user type is inspected
- **THEN** it does not depend on file or stream I/O types
