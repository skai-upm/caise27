# singleton-controllers-and-injected-repositories Specification

## Purpose

Makes the controllers that represent unique management responsibilities singletons and wires them, the repositories and their collaborations by hand-written dependency injection at start-up (source F3-C03, section 5).

## Requirements

### Requirement: Singleton controllers
Each controller that represents a unique management responsibility (users, system settings, groups, tickets, passes, attractions, invoices) SHALL apply the Singleton pattern, returning the same instance on every request during an execution.

#### Scenario: Repeated access returns the same controller
- **WHEN** a controller instance is requested twice
- **THEN** the same object is returned and it has no public constructor

### Requirement: Injected, replaceable repositories
Repositories SHALL NOT be global singletons; they SHALL be created and configured during application initialization and injected into the elements that use them, through a business-level abstraction that allows different persistence implementations or paths.

#### Scenario: Normal and fly runs differ only in configuration
- **WHEN** the application starts normally and with `--fly`
- **THEN** the same controller code runs with repositories configured for different directories

#### Scenario: Alternative repository implementation
- **WHEN** a test injects a different implementation of the repository abstraction
- **THEN** controllers use it without code changes

### Requirement: Injected controller collaborations
Collaborations between controllers SHALL be established during initialization by injection rather than created inside operations, without a dependency injection framework.

#### Scenario: Collaborators configured at start-up
- **WHEN** the composition code and `pom.xml` are inspected
- **THEN** controllers receive their collaborators in a configuration step and no DI framework dependency exists
