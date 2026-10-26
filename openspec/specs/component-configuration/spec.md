# component-configuration Specification

## Purpose

Defines how the application's components are created and connected: conceptually unique controllers are singletons, repositories are ordinary objects created and injected at startup, and collaborations between controllers are configured during initialisation.

## Requirements

### Requirement: Singleton controllers
Each controller representing a single management responsibility (system state, users, groups, tickets, attractions, invoices) SHALL guarantee a single instance through the Singleton pattern: it SHALL NOT be instantiable from outside its class and SHALL be obtained through a static access method that always returns the same instance.

#### Scenario: Same instance
- **WHEN** a controller's access method is called twice
- **THEN** both calls return the same object

#### Scenario: No public constructor
- **WHEN** the controller classes are inspected
- **THEN** none of them exposes a public constructor

### Requirement: Repositories injected at startup
Repositories SHALL NOT be singletons. They SHALL be created and configured (with their file locations) during application startup and injected into the controllers that use them; a controller SHALL NOT create its own repository.

#### Scenario: Repository supplied by initialisation
- **WHEN** the application starts
- **THEN** it creates each repository with the paths of the selected data directory and passes it to the controller that uses it

#### Scenario: Several repository instances possible
- **WHEN** two repositories of the same class are created with different paths
- **THEN** they are distinct objects working on different files

### Requirement: Controller dependencies configured at initialisation
When a controller needs another controller or service, the dependency SHALL be provided during application initialisation and stored by the controller, rather than being created or looked up inside the operation that uses it. A controller used before being configured SHALL fail with a clear error instead of working with missing dependencies.

#### Scenario: Group controller uses the configured user controller
- **WHEN** the application is initialised
- **THEN** the group controller holds the user controller it was given and uses it to find members

#### Scenario: Unconfigured controller
- **WHEN** an operation is called on a controller that has not been configured
- **THEN** it fails with an error stating that the controller is not configured

### Requirement: Execution modes through configuration
Normal and `--fly` executions SHALL use the same controller logic, differing only in the repository instances (and their paths) injected at startup.

#### Scenario: Fly mode
- **WHEN** the application starts with `--fly`
- **THEN** the controllers are configured with repositories pointing to the temporary directory and behave exactly as in normal mode
