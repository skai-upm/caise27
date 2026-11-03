# Spec Delta

## Purpose

Represents park users as independent, encapsulated objects holding the data later user operations require.

## ADDED Requirements

### Requirement: User attributes
A user SHALL hold first name, surname(s), DNI, email address, password, telephone number and card number, accessible only through the user's own interface (attributes are encapsulated).

#### Scenario: All attributes available
- **WHEN** a user is created with all seven values
- **THEN** each value can be read back unchanged

### Requirement: Immutable DNI
A user's DNI SHALL NOT be changeable after the user is created.

#### Scenario: DNI cannot be modified
- **WHEN** any operation attempts to change an existing user's DNI
- **THEN** the change is rejected and the DNI keeps its original value

### Requirement: Separation of responsibilities
The user representation, application start-up and user storage SHALL be handled by separate classes; neither the user class nor the main class SHALL perform user file reading or writing.

#### Scenario: Architecture inspection
- **WHEN** the code is inspected
- **THEN** user file reading and writing exist only in a dedicated persistence class
