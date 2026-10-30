# application-architecture Specification

## Purpose

Defines the structural rules of the application: layered packages, exception-based handling of abnormal situations, Singleton controllers and hand-written dependency injection of repositories and collaborators.

## Requirements

### Requirement: Layered packages
Production code SHALL be organised in separate packages for presentation (user interaction and command processing), business (model, rules, controllers) and persistence (repositories and file I/O) under the root package `es.upm.amusementpark`; the default package SHALL NOT be used. Presentation SHALL NOT access persistence files, and domain objects SHALL NOT store or load themselves.

#### Scenario: Presentation does not depend on file persistence
- **WHEN** the classes of the presentation package are inspected
- **THEN** none of them references the persistence package or the park data files; the only file they read is the command input file

### Requirement: Exceptions for abnormal situations
Rejected or impossible operations SHALL be signalled with exceptions from the application's own exception hierarchy and handled at the presentation level, which reports them as `ERROR:` messages without stopping command processing.

#### Scenario: Rejected operation reported and processing continues
- **WHEN** a command file contains an invalid USER CREATE followed by a valid one
- **THEN** an `ERROR:` message is printed for the first and the second user is created

### Requirement: Singleton controllers
Each controller representing a unique management responsibility (central park, users, groups, tickets, passes, attractions, invoices, system settings) SHALL have exactly one instance per execution. Repositories SHALL NOT be Singletons.

#### Scenario: Controller uniqueness
- **WHEN** a controller instance is requested twice
- **THEN** the same instance is returned

#### Scenario: Repositories are distinct per configuration
- **WHEN** the application is initialised twice with different data directories
- **THEN** each initialisation creates new repository instances bound to its own directory

### Requirement: Dependency injection without frameworks
Repositories SHALL be created during application initialisation and injected into the controllers that use them, and controller collaborations SHALL be configured there too, using project code only (no DI framework). Normal and `--fly` modes SHALL differ only in the repository paths injected.

#### Scenario: Same controllers, different storage
- **WHEN** the same command sequence runs in normal mode and in `--fly` mode
- **THEN** the outputs are identical and only the storage location differs
