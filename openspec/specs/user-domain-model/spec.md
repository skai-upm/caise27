# user-domain-model Specification

## Purpose

Introduces the park user as an independent, encapsulated domain object holding the data required by later user operations (source F1-C01, sections 3.1 and 3.2).

## Requirements

### Requirement: User attributes
A user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number, each accessible through the user's public interface and not through directly exposed fields.

#### Scenario: Created user exposes all attributes
- **WHEN** a user is created with values for all seven attributes
- **THEN** each of the seven values can be read back unchanged

### Requirement: DNI is immutable
A user's DNI SHALL NOT be modifiable after the user has been created; no operation of the system SHALL change the DNI of an existing user.

#### Scenario: DNI cannot be changed
- **WHEN** any operation attempts to change the DNI of an existing user
- **THEN** the change is rejected or impossible and the user keeps the original DNI

### Requirement: Separation of user, application and persistence responsibilities
The system SHALL keep representing a user, starting/running the application, and storing/retrieving users in separate classes; the user representation SHALL NOT perform persistence and the application entry point SHALL NOT contain file reading or writing logic.

#### Scenario: Responsibilities are separated
- **WHEN** the source code is inspected
- **THEN** the user class contains no file I/O and the main class contains no file reading or writing logic for user data
