# Spec Delta

## Purpose

Guarantees one instance of each controller with a unique management responsibility and supplies replaceable repositories and controller collaborations by injection during application initialization.

## ADDED Requirements

### Requirement: Singleton controllers
Controllers that represent a unique management responsibility (park management and invoice generation) SHALL apply the Singleton pattern, so the same instance is returned whenever one is requested during an execution.

#### Scenario: Repeated access
- **WHEN** a controller is requested several times during one run
- **THEN** the same instance is returned each time

### Requirement: Injected, replaceable repositories
Repositories SHALL be defined behind interfaces, SHALL NOT be singletons, and SHALL be created and configured during application initialization and injected into the controllers that use them. Collaborations between controllers SHALL be configured at initialization. Dependency injection SHALL be implemented with project code, without frameworks.

#### Scenario: Normal and --fly configuration
- **WHEN** the application starts in normal mode and in `--fly` mode
- **THEN** the controllers receive repositories configured with different paths and their business logic is unchanged

#### Scenario: Alternative repository implementation
- **WHEN** a controller is configured with a different implementation of the repository interfaces (for example in tests)
- **THEN** the controller works without changes to its code
