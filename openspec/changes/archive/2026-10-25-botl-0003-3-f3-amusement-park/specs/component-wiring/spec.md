# Spec Delta

## Purpose

Defines how controllers, repositories and their collaborations are instantiated and connected at application startup.

## ADDED Requirements

### Requirement: Singleton controllers
Each controller representing a single management responsibility (users, groups, tickets and passes, attractions, invoices, and the main park controller) SHALL guarantee a single instance through the Singleton pattern.

#### Scenario: Same instance
- **WHEN** a controller's instance accessor is called twice
- **THEN** the same object is returned

### Requirement: Repositories injected at startup
Repositories SHALL NOT be Singletons. They SHALL be created and configured with their file locations during application initialisation and injected into the controllers that use them.

#### Scenario: Different paths without controller changes
- **WHEN** the application runs normally and with `--fly`
- **THEN** the same controller code works with repositories configured for `data/` and for the temporary directory respectively

### Requirement: Collaborations wired at initialisation
When a controller needs another controller or service, the dependency SHALL be provided during application initialisation rather than constructed inside the operation that uses it.

#### Scenario: Wiring
- **WHEN** the application starts
- **THEN** all controller dependencies are configured before the first command is processed
