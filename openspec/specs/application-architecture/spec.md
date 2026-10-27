# application-architecture Specification

## Purpose

Defines the structural rules of the application: the layered package organisation (presentation, business, persistence) with the responsibilities of each layer, and how exceptional situations are represented, propagated and handled.

## Requirements

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

### Requirement: Singleton controllers
The controllers responsible for a unique management function during an execution — the park controller and the user, group, ticket, attraction and invoice controllers — SHALL each have exactly one instance, guaranteed by the Singleton pattern regardless of where they are requested.

#### Scenario: One instance per controller
- **WHEN** any of these controllers is requested several times from different parts of the application
- **THEN** the same instance is returned and the class offers no public constructor

### Requirement: Injected replaceable repositories
Repositories SHALL NOT be singletons. Repository instances SHALL be created and configured during application initialization and injected into the controllers that use them; collaborations between controllers SHALL also be established at initialization. Dependency injection SHALL be implemented with project code, without frameworks.

#### Scenario: Different configurations without changing business logic
- **WHEN** the application is initialized once with repositories stored in `data/` and once with repositories stored in a `--fly` temporary directory
- **THEN** the same controllers operate on the injected repositories of each configuration, with no change to their business logic

#### Scenario: In-memory repositories injected for testing
- **WHEN** the controllers are initialized with in-memory repositories
- **THEN** operations work on those repositories and no files are written

#### Scenario: Repositories are not global singletons
- **WHEN** the repository classes are inspected
- **THEN** they have public constructors and no static instance accessor
