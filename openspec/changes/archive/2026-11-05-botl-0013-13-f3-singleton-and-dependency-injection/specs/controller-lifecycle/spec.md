# Spec Delta

## Purpose

Defines how the application's controllers exist exactly once per execution and how repositories and collaborating components are created at startup and injected, so storage implementations and locations can change without touching controller logic.

## ADDED Requirements

### Requirement: Unique controllers
Each controller with a unique management responsibility — the main park controller and the user, group, ticket and attraction managers — SHALL have exactly one instance per execution, which every element of the application obtains through the controller's single access point; the controllers SHALL NOT be instantiable from outside their class.

#### Scenario: Same instance everywhere
- **WHEN** different application elements request the user manager (or the main controller) during one execution
- **THEN** they all receive the same instance

#### Scenario: No public construction
- **WHEN** the controller classes are inspected
- **THEN** they expose no public constructor

### Requirement: Replaceable repositories
Repositories SHALL be defined by abstractions that controllers depend on, SHALL NOT be singletons, and SHALL be replaceable by another implementation or configuration without changing controller logic.

#### Scenario: Alternative implementation
- **GIVEN** an in-memory implementation of the user repository
- **WHEN** it is injected into the user manager instead of the file repository
- **THEN** user creation, update and deletion behave as specified and no file is written

### Requirement: Startup configuration and injection
Repository instances SHALL be created and configured at application startup and injected into the controllers that use them, and controller-to-controller and controller-to-service collaborations SHALL be established during that initialization; business operations SHALL NOT construct repositories, controllers or collaborating services.

#### Scenario: Normal and --fly configurations
- **WHEN** the application starts once normally and once with `--fly`
- **THEN** the repositories injected at startup use the working-directory files in the first case and the run's temporary directory in the second, and the same controller instances and logic serve both

#### Scenario: No local construction in operations
- **WHEN** the business operations of the controllers are inspected
- **THEN** none of them creates a repository, a manager, the main controller or the invoice director
