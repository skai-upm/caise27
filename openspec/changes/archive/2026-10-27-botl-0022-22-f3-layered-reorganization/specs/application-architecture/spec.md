# Spec Delta

## Purpose

Defines the structural rules of the application: the layered package organisation (presentation, business, persistence) with the responsibilities of each layer, and how exceptional situations are represented, propagated and handled.

## ADDED Requirements

### Requirement: Layered packages
The code SHALL be organised in separate Java packages for presentation (user interaction and command processing), business (model, business rules and application control) and persistence (repositories and reading/writing persistent information), all under the root package `es.upm.amusementpark`; the default package SHALL NOT be used.

#### Scenario: Layer packages present
- **WHEN** the production sources are inspected
- **THEN** every class belongs to `es.upm.amusementpark.presentation`, `es.upm.amusementpark.business` (or one of its subpackages) or `es.upm.amusementpark.persistence`, and none is in the default package or directly in the root package

### Requirement: Layer responsibilities respected
The presentation layer SHALL NOT access persistence files or file storages directly, and domain objects SHALL NOT store or retrieve their persistent representations.

#### Scenario: Presentation does not touch persistence
- **WHEN** the presentation classes are inspected
- **THEN** they do not reference persistence classes or file APIs for data files (only the command file given by the user is read)

#### Scenario: Domain objects free of persistence
- **WHEN** the model classes are inspected
- **THEN** they do not reference persistence classes or file APIs

### Requirement: Exceptions represent abnormal situations
Abnormal situations in operations SHALL be represented and propagated with exceptions — business rule violations, persistence failures and malformed commands — and SHALL be handled in a controlled manner at the command processing level, so that an incorrect command or an operation that cannot be completed never terminates command processing.

#### Scenario: Business rule violation
- **WHEN** an operation violates a business rule (for example a duplicate DNI)
- **THEN** the business layer throws an exception, the command layer reports it as an error and continues with the next command

#### Scenario: Malformed command
- **WHEN** a command has the wrong number of elements or an invalid value (date, price, capacity, field)
- **THEN** an exception describing the problem is raised during parsing and reported as an error, and processing continues
