# application-architecture Specification

## Purpose

Defines how the application is organised into presentation, business and persistence layers, how abnormal situations are propagated with exceptions, and how unique controllers and injected dependencies are wired.

## Requirements

### Requirement: Layered packages
The code SHALL be organised in separate Java packages for presentation (user interaction and command processing), business (model, rules and controllers) and persistence (repositories and file access) under the root package; the default package MUST NOT be used. Presentation MUST NOT access persistence files directly and domain objects MUST NOT store or retrieve their own persistent representation.

#### Scenario: Packages present
- **WHEN** the source tree is inspected
- **THEN** it contains `presentation`, `business` and `persistence` packages under `es.upm.etsisi.amusementpark` and no class in the default package

#### Scenario: Presentation independent of persistence
- **WHEN** the presentation classes are inspected
- **THEN** none of them imports a persistence class or performs file I/O on data files

### Requirement: Exceptions for abnormal situations
Operations SHALL signal abnormal situations by throwing exceptions from a project exception hierarchy; they MUST be handled at the presentation level so that an incorrect command or failed operation never terminates command processing unexpectedly.

#### Scenario: Failed operation reported
- **WHEN** an operation fails (for example deleting an unknown user)
- **THEN** a business exception is raised, the presentation layer shows an error and processing continues with the next command

### Requirement: Singleton controllers
Controllers representing a unique management responsibility during an execution SHALL apply the Singleton pattern, so that only one instance of each exists. Repositories MUST NOT be singletons.

#### Scenario: Unique controllers
- **WHEN** each controller's instance is requested twice
- **THEN** the same instance is returned and no public constructor exists

### Requirement: Dependency injection without frameworks
Repository instances SHALL be created and configured during application initialisation and injected into the controllers that use them, and collaborations between controllers SHALL be configured at initialisation, using project code only (no DI framework). Normal and `--fly` modes MUST differ only in the injected repository configuration.

#### Scenario: Replaceable repositories
- **WHEN** the controllers are configured with in-memory test repositories instead of text-file repositories
- **THEN** the business operations work unchanged
