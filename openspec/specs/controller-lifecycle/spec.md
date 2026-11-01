# controller-lifecycle Specification

## Purpose

Guarantees a single instance of each conceptually unique controller per execution and wires repositories and collaborating controllers at start-up through dependency injection.

## Requirements

### Requirement: Singleton controllers
Each controller representing a single management responsibility (system settings, users, park operations, invoices) SHALL have exactly one instance per execution, obtained through the Singleton pattern; requesting it again SHALL return the same instance, and initialising it a second time in the same execution SHALL be rejected.

#### Scenario: Same instance on repeated access
- **GIVEN** the application has been initialised
- **WHEN** a controller instance is requested twice
- **THEN** both requests return the same object

### Requirement: Injected, replaceable repositories
Repositories SHALL be defined by interfaces, SHALL NOT be Singletons, and SHALL be created and configured at application start-up and injected into the controllers that use them, so different implementations or storage paths can be used without changing controller logic.

#### Scenario: Normal and fly wiring
- **GIVEN** a normal start-up and a `--fly` start-up
- **WHEN** repositories are wired
- **THEN** the same controller classes receive repositories configured with different paths and behave identically

#### Scenario: Alternative repository implementation
- **GIVEN** an in-memory implementation of a repository interface
- **WHEN** it is injected into a controller
- **THEN** the controller operates without modification

### Requirement: Collaborators configured at initialisation
Dependencies between controllers and services (for example the park controller using the system settings and user controllers, or the user controller using the password hasher) SHALL be established during application initialisation rather than constructed inside the operations that use them.

#### Scenario: Wiring at start-up
- **WHEN** the application starts
- **THEN** every controller receives its collaborators before the first command is processed
