# Spec Delta

## Purpose

Defines how the application is organized in layers — presentation, business and persistence — the dependency rules between them, and how exceptions represent and propagate abnormal situations.

## ADDED Requirements

### Requirement: Layered packages
The code SHALL be organized in separate Java packages for presentation (user interaction and command processing), business (model, business rules and application control) and persistence (repositories and reading/writing persistent information), under the root package `es.upm.amusementpark`; no class SHALL be in the default package.

#### Scenario: Package layout
- **WHEN** the production sources are inspected
- **THEN** every class is in `es.upm.amusementpark.presentation`, `es.upm.amusementpark.business` (or a subpackage) or `es.upm.amusementpark.persistence` (or a subpackage)

### Requirement: Layer dependencies
The presentation layer SHALL NOT access persistence files, directories or storage classes directly, and domain objects SHALL NOT store or retrieve their persistent representation.

#### Scenario: Presentation imports
- **WHEN** the imports of the presentation classes are inspected
- **THEN** none refers to `es.upm.amusementpark.persistence`, `java.nio.file.Files` or the domain-object persistence

#### Scenario: Domain imports
- **WHEN** the imports of the domain model classes are inspected
- **THEN** none refers to the persistence layer or to file I/O

### Requirement: Behavior preserved
The reorganization SHALL NOT change the observable behavior of any Delivery 1 or Delivery 2 command, execution mode or persisted data format.

#### Scenario: Previous integration sessions
- **WHEN** the Delivery 1 and Delivery 2 integration sessions are run after the reorganization
- **THEN** they produce the same responses and data files as before

### Requirement: Exceptions for abnormal situations
Abnormal situations SHALL be represented by exceptions of a dedicated hierarchy (invalid data, element not found, conflict with existing data, operation not allowed by the rules, persistence failure, malformed command), thrown where the situation is detected and handled in the presentation layer, which prints one `ERROR:` line and continues processing the next command.

#### Scenario: Rejected operation continues processing
- **WHEN** a command fails because of invalid data, a missing element, a conflict or a forbidden operation
- **THEN** an `ERROR:` line describing the cause is printed and the following command is processed normally

#### Scenario: Exception kinds
- **WHEN** `TICKET DELETE;T999` names no ticket, `USER CREATE` repeats an existing DNI, and `GROUP ADD` targets a locked group
- **THEN** the business layer signals respectively a not-found, a conflict and an operation-not-allowed exception, all reported as `ERROR:` lines
