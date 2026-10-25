# Spec Delta

## Purpose

Defines how controllers are instantiated and how repositories and collaborators are supplied to them.

## ADDED Requirements

### Requirement: Singleton controllers
Each controller that represents a single management responsibility (system settings, users, groups, tickets, passes, attractions, invoices) SHALL guarantee a single instance through the Singleton pattern.

#### Scenario: Same instance
- **WHEN** a controller instance is requested twice
- **THEN** the same object is returned

### Requirement: Repositories injected at startup
Repositories SHALL be defined by abstractions, SHALL NOT be Singletons, and SHALL be created and configured at application startup and injected into the controllers that use them. Normal and `--fly` executions SHALL differ only in the repository paths configured at startup, without changes to controller logic.

#### Scenario: Different paths, same controllers
- **WHEN** the application starts once normally and once with `--fly`
- **THEN** the same controller classes operate on repositories configured with `data/` and `tmp/<timestamp>/` paths respectively

### Requirement: Collaborations configured during initialization
When a controller depends on another controller or service, the dependency SHALL be configured during application initialization instead of being constructed inside the operation that uses it.

#### Scenario: Initialization wiring
- **WHEN** the application initializes
- **THEN** every controller receives its repositories and collaborating controllers before any command is processed
