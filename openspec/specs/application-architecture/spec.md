# application-architecture Specification

## Purpose

Constrains the structure of the application: layered packages, exception-based error handling, unique controllers and injected dependencies.

## Requirements

### Requirement: Layered packages
Code SHALL be organised in distinct presentation (user interaction, command processing), business (model, rules, control) and persistence (repositories, file I/O) packages, never the default package. Presentation SHALL NOT access persistence files directly and domain objects SHALL NOT read or write their own persistent representation.

#### Scenario: Package inspection
- **WHEN** the source tree is inspected
- **THEN** every class lies under a presentation, business or persistence package, except the application entry point in the root package

### Requirement: Exceptions for abnormal situations
Operations that cannot be completed SHALL signal it with exceptions that are handled at the presentation level, printing an error and continuing with the next command.

#### Scenario: Failing command continues
- **WHEN** a command fails because of a business rule
- **THEN** an `ERROR:` message is printed and the next command is processed

### Requirement: Unique controllers
Controllers representing a unique management responsibility SHALL have a single instance per execution, obtained from anywhere in the application.

#### Scenario: Same controller instance
- **WHEN** a controller is requested twice
- **THEN** the same instance is returned

### Requirement: Injected repositories and collaborators
Repositories SHALL NOT be singletons; they SHALL be created and configured during application initialization (normal or `--fly` paths) and injected, as SHALL collaborations between controllers, without dependency-injection frameworks.

#### Scenario: Reconfigured persistence
- **WHEN** the application is initialised twice with different data directories
- **THEN** controllers keep their identity but operate on repositories bound to the new directory
