# application-architecture Specification

## Purpose

Defines the structural contract of the application: layered packages with clear responsibility boundaries, single-instance controllers per execution, and repositories configured at startup and injected so that storage can change without touching business logic.

## Requirements

### Requirement: Layered packages
Java code SHALL be organised into distinct `presentation`, `business` and `persistence` packages (with sub-packages where useful) and SHALL NOT use the default package. Presentation SHALL handle command interaction; business SHALL hold the model, rules and controllers; persistence SHALL hold repositories and file access.

#### Scenario: Package layout
- **WHEN** the source tree is inspected
- **THEN** every class belongs to a package under `es.upm.amusementpark.presentation`, `es.upm.amusementpark.business` or `es.upm.amusementpark.persistence`

### Requirement: Layer boundaries
Command handling SHALL NOT read or write persistence files directly, and domain model classes SHALL NOT perform file reading or writing; all data-file access SHALL be performed by persistence classes.

#### Scenario: No file access outside persistence
- **WHEN** presentation and business classes are inspected
- **THEN** none of them opens, reads, writes or deletes data files; command files are only read as command input by presentation

### Requirement: Behavior preserved by restructuring
All E1 and E2 commands SHALL keep their syntax and observable results, and `mvn package` SHALL produce the executable JAR.

#### Scenario: Regression after restructuring
- **WHEN** the E1/E2 regression suite runs after the reorganisation
- **THEN** every test passes and `mvn package` succeeds

### Requirement: Singleton controllers
Each controller representing a unique management responsibility (park, users, groups, tickets, attractions) SHALL have a single instance per execution: it SHALL be created once at startup and every later access SHALL return that same instance; a second initialisation within the same execution SHALL be rejected.

#### Scenario: Same instance within an execution
- **WHEN** a controller is obtained several times during one execution
- **THEN** the same instance is returned each time

### Requirement: Injected repositories and collaborators
Repositories SHALL be defined as replaceable abstractions, SHALL NOT be Singletons, and SHALL be created and configured at application startup and injected into the controllers that use them, together with any controller or service a controller collaborates with. Normal and `--fly` executions SHALL differ only in the configured repository paths.

#### Scenario: Alternative repository injected
- **WHEN** a controller is initialised with an in-memory repository implementation instead of the file one
- **THEN** its business operations work without modification

#### Scenario: Fly wiring
- **WHEN** the application starts with `--fly`
- **THEN** the same controller classes are initialised with file repositories pointing to the temporary directory
