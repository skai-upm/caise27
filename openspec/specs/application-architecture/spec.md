# application-architecture Specification

## Purpose

Defines the structural constraints of the application: layered packages, single-instance controllers, and repositories plus collaborators configured at start-up and injected, so persistence locations can change without touching business logic.

## Requirements

### Requirement: Layered packages
The code SHALL be organised in separate Java packages for presentation (command interaction), business (model, domain rules, controllers) and persistence (repositories and file access), with subpackages where useful. No class SHALL be in the default package.

#### Scenario: Package inspection
- **WHEN** the source tree is inspected
- **THEN** classes are located under `es.upm.parque.presentation`, `es.upm.parque.business` and `es.upm.parque.persistence` (plus the bootstrap in `es.upm.parque`), and none in the default package

### Requirement: Separation of persistence
Command handling SHALL NOT access persistence files directly and domain objects SHALL NOT read or write files; only persistence-layer classes perform file I/O.

#### Scenario: Presentation does not touch data files
- **WHEN** the presentation and business packages are inspected
- **THEN** they contain no reading or writing of data files

### Requirement: Behavior preserved
The reorganisation SHALL NOT change the syntax or behavior of any E1 or E2 command or execution mode.

#### Scenario: Existing commands still work
- **WHEN** the E1 and E2 command scenarios are executed after the reorganisation
- **THEN** they produce the same results as before

### Requirement: Singleton controllers
Controllers that represent a single management responsibility (system settings, users, groups, tickets, passes, attractions, invoices) SHALL guarantee a single instance through the Singleton pattern.

#### Scenario: Single instance
- **WHEN** a controller instance is requested twice
- **THEN** the same instance is returned

### Requirement: Injected repositories and collaborators
Repositories SHALL NOT be singletons. They SHALL be created and configured at start-up with their file locations and injected into the controllers that use them; dependencies between controllers SHALL also be established during initialisation rather than constructed inside operations.

#### Scenario: Normal and fly paths
- **WHEN** the application starts in normal mode or with `--fly`
- **THEN** the same controller logic runs with repositories configured for the permanent or the temporary directory respectively
