# singleton-controllers-and-injected-repositories Specification

## Purpose

Defines the unique controllers implemented as Singletons and the hand-written dependency injection of repositories and controller collaborations (F3-C03).

## Requirements

### Requirement: Singleton controllers
Controllers representing a unique management responsibility during an execution SHALL apply the Singleton pattern, returning the same instance every time they are requested during one run.

#### Scenario: Repeated access
- **WHEN** a controller is requested twice during one execution
- **THEN** the same instance is returned and it has no public constructor

### Requirement: Injected replaceable repositories
Repositories SHALL NOT be global singletons. They SHALL be created and configured during application initialisation and injected into the components that need them, as should collaborations between controllers, using project code only (no dependency injection framework). Normal and `--fly` executions SHALL differ only in the repository configuration, not in controller logic.

#### Scenario: Different configuration, same controllers
- **WHEN** the application is initialised for normal mode and for `--fly` mode
- **THEN** the same controller classes are wired with repositories configured for different directories

#### Scenario: Replaceable repository
- **WHEN** the park state repository is replaced by an in-memory implementation in tests
- **THEN** the controllers work unchanged
