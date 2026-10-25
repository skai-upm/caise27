# application-architecture Specification

## Purpose

Defines how the application's code is organised into presentation, business and persistence layers and which dependencies between them are allowed.

## Requirements

### Requirement: Layered packages
The production code SHALL be organised into separate Java packages for the presentation layer (command-based interaction), the business layer (domain model, domain rules and controllers, in sub-packages) and the persistence layer (repositories and file access). No class SHALL be in the default package.

#### Scenario: Packages present
- **WHEN** the source tree is inspected
- **THEN** it contains the packages `es.upm.parque.presentation`, `es.upm.parque.business.model`, `es.upm.parque.business.controller` and `es.upm.parque.persistence`, and no source file lacks a package declaration

### Requirement: Layer dependencies
Presentation classes SHALL NOT access persistence classes or files directly; business model classes SHALL NOT read or write files nor depend on persistence or presentation; persistence classes SHALL depend only on the business model. Only the startup configuration creates repositories and wires them into controllers.

#### Scenario: Commands do not access files
- **WHEN** the imports of the presentation and business model packages are inspected
- **THEN** none of them references `es.upm.parque.persistence`, `java.nio.file.Files` or other file I/O classes

### Requirement: Behavior preserved
The reorganisation SHALL NOT change the syntax or behavior of any Delivery 1 or Delivery 2 command, the stored file formats or the execution modes, and `mvn package` SHALL still produce the executable JAR.

#### Scenario: Existing commands still work
- **WHEN** the full test suite and a command file exercising users, groups, tickets, group tickets, attractions, `SYSTEM DATE`, `--fly`, `HELP` and `EXIT` are run after the reorganisation
- **THEN** all tests pass and the responses are identical to those before it

### Requirement: Unique controllers
Each controller representing a single management responsibility (users, groups, tickets, attractions, invoices) SHALL have exactly one instance in the application, obtained through a Singleton access point; it SHALL NOT be instantiable with `new` from other classes.

#### Scenario: Same controller everywhere
- **WHEN** the application has started and the user controller is obtained twice through its access point
- **THEN** both references are the same object, which is the one used by the command processor

### Requirement: Dependencies injected at startup
Controllers SHALL receive the repositories, the shared system date and the other controllers they collaborate with when they are initialised at application startup, and SHALL NOT construct those collaborators inside their operations.

#### Scenario: Collaborators configured at initialisation
- **WHEN** the ticket controller is initialised with a given user controller and group controller
- **THEN** ticket operations use exactly those instances

### Requirement: Replaceable repositories
Repositories SHALL NOT be Singletons. Controllers SHALL depend on repository abstractions, so different implementations or configurations (such as the permanent directory and the `--fly` temporary directory) can be injected without changing controller logic.

#### Scenario: Normal and fly modes
- **WHEN** the application starts normally and with `--fly`
- **THEN** the same controller code runs with repositories configured for `data/` or for the temporary directory respectively

#### Scenario: Alternative implementation
- **WHEN** a controller is initialised with an in-memory implementation of its repository abstraction in a test
- **THEN** it works without any change to the controller
