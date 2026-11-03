# Spec Delta

## Purpose

Guarantees unique controllers for unique management responsibilities while keeping repositories replaceable dependencies injected at start-up.

## ADDED Requirements

### Requirement: Singleton controllers
Controllers representing a unique management responsibility SHALL apply the Singleton pattern so that one instance exists per execution.

#### Scenario: Same controller instance
- **WHEN** a controller is requested repeatedly during one run
- **THEN** the same instance is returned

### Requirement: Injected replaceable repositories
Repositories SHALL NOT be singletons; they SHALL be created and configured during application initialisation and injected into the controllers that use them, and collaborations between controllers SHALL be configured by injection, using project code without a dependency injection framework.

#### Scenario: Normal and fly configuration
- **WHEN** the application starts normally and with `--fly`
- **THEN** the same controller logic runs with repositories configured for different paths

#### Scenario: Alternative repository implementation
- **WHEN** a controller is initialised with a different repository implementation
- **THEN** it works without changes to its business logic
