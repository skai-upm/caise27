# Spec Delta

## Purpose

Defines how the application's code is organized into presentation, business and persistence layers, which dependencies between layers are allowed, and how abnormal situations are represented with exceptions and handled.

## ADDED Requirements

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
