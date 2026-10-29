# architecture Specification

## Purpose

Defines how the application's code is organized into presentation, business and persistence layers, which dependencies between layers are allowed, and how abnormal situations are represented with exceptions and handled.

## Requirements

### Requirement: Layered packages
The application SHALL have separate Java packages, inside a named root package, for presentation (user interaction and command processing), business (model, business rules and application control) and persistence (repositories and reading and writing persistent information). No class SHALL be in the default package.

#### Scenario: Packages present
- **WHEN** the production sources are inspected
- **THEN** command processing is in a presentation package, the model and control classes are in business packages, the repositories are in a persistence package, and every class declares a package under the root package

### Requirement: Layer dependency rules
The presentation layer SHALL NOT access persistence files or persistence classes directly; the business layer SHALL NOT depend on the presentation or persistence layers; domain objects SHALL NOT store or retrieve their own persistent representation. Only the application's composition root MAY reference all layers to wire them together.

#### Scenario: Dependency check
- **WHEN** the imports of each production class are inspected
- **THEN** no presentation class imports a persistence class, no business class imports a presentation or persistence class, and no business class performs file input or output

### Requirement: Exceptions represent abnormal situations
Abnormal situations in business and persistence operations SHALL be represented with exceptions that are propagated to the level able to handle them; the presentation layer SHALL handle them per command by reporting an error, and an incorrect command or an operation that cannot be completed SHALL NOT terminate command processing.

#### Scenario: Business rule violation reported and processing continues
- **WHEN** `GROUP CREATE;G01;A` is processed twice and then `HELP` is processed
- **THEN** the second creation is reported as an error caused by a duplicate-element exception and the help text is still shown

#### Scenario: Distinct exception types
- **WHEN** an operation fails because an element does not exist, an element already exists, a value is invalid, or a business rule forbids it
- **THEN** a corresponding specific exception type, sharing a common business exception supertype, is raised

### Requirement: Behavior preserved
The reorganization SHALL preserve the externally observable behavior reached at the end of Delivery 2, including command syntax, messages and execution modes.

#### Scenario: Existing tests still pass
- **WHEN** the full test suite of Delivery 2 behavior is run after the reorganization
- **THEN** all tests pass with unchanged expected output

### Requirement: Singleton controllers
Each controller whose responsibility is a unique management function during an execution SHALL apply the Singleton pattern: it SHALL have no public constructor and SHALL be obtained through a single access point that always returns the same instance. Persistence components SHALL NOT be singletons.

#### Scenario: Unique controllers
- **WHEN** central park management or invoice control is requested several times
- **THEN** the same instance is returned each time and no public constructor exists

#### Scenario: Repositories are not singletons
- **WHEN** repositories are created for two different directories
- **THEN** two independent repository sets exist, each using its own directory

### Requirement: Injected replaceable repositories
Repository instances SHALL be created and configured during application initialization and injected into the controllers that need them; controllers SHALL depend only on repository abstractions defined in the business layer, so that normal and `--fly` executions use the same repository implementations configured with different paths without changing controller logic. Dependency injection SHALL be implemented with project code, without frameworks.

#### Scenario: Same controller, different repository configuration
- **WHEN** the controller is configured once with repositories for directory A and once with repositories for directory B
- **THEN** its state is loaded from and saved to the configured directory only, using unchanged business logic

#### Scenario: Replaceable implementation
- **WHEN** an in-memory implementation of the repository abstractions is injected instead of the text-file one
- **THEN** the controller saves and reloads its state through it

### Requirement: Injected collaborations
When a controller collaborates with another controller or service, the collaboration SHALL be established during application initialization rather than by creating the collaborator inside each operation.

#### Scenario: Invoice control collaborators
- **WHEN** an invoice is requested
- **THEN** it is produced by the invoice controller using the central park management and invoice director instances injected at initialization, without creating a new director in the operation
