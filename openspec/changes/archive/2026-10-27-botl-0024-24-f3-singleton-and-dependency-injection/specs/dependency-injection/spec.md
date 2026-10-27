# Spec Delta

## Purpose

Defines how unique management controllers are kept as single instances and how repositories and collaborating components are created at initialization and injected, without a dependency-injection framework.

## ADDED Requirements

### Requirement: Singleton controllers
Each controller with a unique management responsibility (park, users, groups, tickets, attractions, invoices) SHALL have exactly one instance per execution, obtained through a static accessor and not constructible elsewhere.

#### Scenario: Same controller instance
- **WHEN** a controller is requested twice from different parts of the application
- **THEN** the same instance is returned and the class has no public constructor

### Requirement: Repositories injected, not global
Repositories SHALL NOT be singletons; their instances SHALL be created and configured during application initialization and injected into the controllers that need them.

#### Scenario: Different configurations, same business logic
- **WHEN** one execution starts in permanent mode and another with `--fly`
- **THEN** the same controller code operates on repositories configured with different data paths

#### Scenario: Replaceable repository
- **WHEN** the application is initialised with a substitute user repository
- **THEN** the controllers use the substitute without code changes

### Requirement: Collaborations configured at initialization
When a controller needs another controller or service, that collaborator SHALL be supplied during initialization instead of being created inside operations; injection SHALL be implemented with project code, without dependency-injection frameworks.

#### Scenario: Invoice builder injected
- **WHEN** the application is initialised
- **THEN** the invoice controller receives its builder and repositories from the initializer

#### Scenario: Behaviour preserved
- **WHEN** the Delivery 1–3 regression tests run on the injected configuration
- **THEN** they all pass
