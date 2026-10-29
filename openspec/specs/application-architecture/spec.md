# application-architecture Specification

## Purpose

Defines the structural constraints of the application: layered Java packages, exception-based handling of abnormal situations, singleton controllers, and dependency injection of repositories and collaborators.

## Requirements

### Requirement: Layered packages
The code SHALL be organised into distinct Java packages for presentation (user interaction and command processing), business (model, business rules, controllers) and persistence (repositories and file reading/writing) under a named root package, with nothing in the default package. Presentation SHALL NOT access persistence files directly, and domain objects SHALL NOT store or retrieve their own persistent representations.

#### Scenario: Package separation
- **WHEN** the source tree is inspected
- **THEN** presentation, business and persistence classes live in separate packages, and no presentation class references a persistence class

### Requirement: Exceptions for abnormal situations
Abnormal situations in system operations (invalid data, unknown entities, duplicates, forbidden operations, failed authentication, persistence failures) SHALL be represented by project-defined exceptions, propagated to the presentation layer and handled there in a controlled way. An incorrect command or an operation that cannot be completed SHALL NOT terminate command processing.

#### Scenario: Failed operation reported
- **WHEN** `TICKET CREATE;T001;nadie@correo.com;2027-04-15` is processed for an unknown user
- **THEN** a project exception is raised by the business layer, an error message is printed, and the next command is processed

### Requirement: Singleton controllers
Each controller that represents a unique management responsibility during an execution (park, users, passes, invoices) SHALL apply the Singleton pattern. Repositories SHALL NOT be singletons.

#### Scenario: Unique controller instances
- **WHEN** a controller instance is requested twice
- **THEN** the same instance is returned, and its constructor is not accessible from outside

### Requirement: Dependency injection without frameworks
Repository instances SHALL be created and configured during application initialization and injected into the controllers, and collaborations between controllers SHALL be set up there too. This SHALL use only Java code from the project, without dependency injection frameworks. Normal and `--fly` modes SHALL differ only in the repository configuration they inject.

#### Scenario: Replaceable repositories
- **WHEN** the controllers are initialized with repositories configured for a different directory
- **THEN** the same controller logic reads and writes only that directory
