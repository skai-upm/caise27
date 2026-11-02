# Spec Delta

## Purpose

Ensures that controllers with a unique management responsibility exist once per execution while repositories remain replaceable dependencies configured and injected at start-up.

## ADDED Requirements

### Requirement: Singleton controllers
Each controller representing a unique management responsibility SHALL apply the Singleton pattern so that every request for it during an execution returns the same instance. (F3-C03)

#### Scenario: Repeated access
- **WHEN** a controller is requested twice
- **THEN** the same instance is returned and it cannot be instantiated from outside

### Requirement: Injected replaceable repositories
Repositories SHALL NOT be global singletons; they SHALL be created and configured during application initialization and injected, through hand-written Java code without a dependency-injection framework, into the elements that use them, and collaborations between controllers SHALL also be configured at initialization. (F3-C03)

#### Scenario: Normal and fly configuration
- **WHEN** the application starts normally and with `--fly`
- **THEN** the same controllers run unchanged business logic over repositories configured with different directories

#### Scenario: Alternative repository implementation
- **WHEN** a different implementation of the repository abstraction is injected
- **THEN** the controllers operate on it without modification
