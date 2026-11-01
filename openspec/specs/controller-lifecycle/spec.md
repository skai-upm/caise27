# controller-lifecycle Specification

## Purpose

Guarantees a single instance of each conceptually unique controller per execution and wires repositories and collaborators into them at startup through dependency injection.

## Requirements

### Requirement: Singleton controllers
Each controller representing a unique management responsibility SHALL have exactly one instance per execution, obtained through the Singleton pattern; requesting it repeatedly during an execution SHALL return the same instance.

#### Scenario: Same instance within an execution
- **GIVEN** the application has started
- **WHEN** a unique controller is requested several times
- **THEN** the same instance is returned every time

### Requirement: Injected, replaceable repositories
Repositories SHALL NOT be Singletons. They SHALL be created and configured at application startup and injected into the controllers that use them, so different repository implementations or configurations can be supplied without changing controller logic.

#### Scenario: Alternative repository injected
- **GIVEN** a controller initialized with a repository implementation other than the file-based one
- **WHEN** controller operations run
- **THEN** they use the injected repository without code changes in the controller

### Requirement: Collaborators configured at initialization
Dependencies between controllers and services SHALL be established during application initialization and never constructed inside the operation that uses them.

#### Scenario: Wiring at startup
- **WHEN** the application starts
- **THEN** every controller receives its collaborators before the first command is processed

### Requirement: Mode-independent controllers
Normal and `--fly` executions SHALL differ only in the paths configured for the injected repositories; controller business logic SHALL be identical.

#### Scenario: Normal and fly wiring
- **WHEN** the application starts normally and with `--fly`
- **THEN** the same controller classes are used with repositories pointing to different directories
