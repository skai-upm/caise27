# Spec Delta

## Purpose

Defines how the application is organized in presentation, business and persistence layers, which dependencies between them are allowed, and how abnormal situations are represented with exceptions and handled without stopping command processing.

## ADDED Requirements

### Requirement: Layered packages
The application code SHALL be organized in distinct Java packages under the root package `es.upm.parque`: a presentation package for user interaction and command processing, a business package for the model, business rules and application control, and a persistence package for repositories and reading and writing persistent information. No class SHALL be in the default package.

#### Scenario: Every class belongs to a layer
- **WHEN** the main sources are inspected
- **THEN** every class is declared in `es.upm.parque.presentation`, `es.upm.parque.business` (or a subpackage) or `es.upm.parque.persistence` (or a subpackage), and none is in the default package

### Requirement: Layer dependency rules
The presentation layer MUST NOT access persistence classes or files directly; it SHALL use the business layer only. Domain model classes MUST NOT read or write their persistent representations.

#### Scenario: Presentation does not import persistence
- **WHEN** the presentation sources are inspected
- **THEN** none of them imports a class from `es.upm.parque.persistence` or uses file input/output for park data

#### Scenario: Model performs no persistence
- **WHEN** the `es.upm.parque.business.model` sources are inspected
- **THEN** none of them imports `es.upm.parque.persistence` or `java.nio.file`

### Requirement: Exceptions for abnormal situations
Business operations SHALL signal abnormal situations (missing elements, invalid data, violated business rules) by throwing exceptions of a business exception hierarchy; the presentation layer SHALL signal malformed commands with its own exception, and SHALL catch these exceptions at the command-processing level, print a response starting with `ERROR` with the exception's message, and continue with the next command.

#### Scenario: Business rule violation becomes an error response
- **WHEN** `USER CREATE` is processed with a DNI that already exists
- **THEN** the business operation throws a rule-violation exception and the printed response is `ERROR: a user with DNI <dni> already exists`

#### Scenario: Processing continues after an exception
- **WHEN** a command file contains a command that fails with an exception followed by a valid command
- **THEN** the failing command produces an `ERROR` response and the next command is processed normally

### Requirement: Behavior preserved by the reorganization
The reorganization SHALL NOT change the externally observable behavior established by Delivery 2: commands, responses, persisted file formats, temporary mode and the executable JAR entry point keep working as before.

#### Scenario: Existing behavior still holds
- **WHEN** the full test suite from Delivery 2, moved to the new packages, runs against the reorganized code
- **THEN** all tests pass and `java -jar target/amusement-park.jar` with the Delivery 2 command file produces the same output as before the reorganization
