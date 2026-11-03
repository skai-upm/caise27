# component-lifecycle Specification

## Purpose

Defines how the application's components are created and connected: unique controllers per execution, replaceable repositories, and all dependencies configured at startup.

## Requirements

### Requirement: Unique controllers
Each controller with an application-wide management responsibility (users, groups, tickets, attractions and the main park controller) SHALL have exactly one instance per execution, created when the application is initialised and obtained by every other element through the controller's single access point; its constructor SHALL NOT be accessible to other classes.

#### Scenario: Same instance everywhere
- **GIVEN** the application has been initialised
- **WHEN** the command layer, the main controller and other controllers obtain the user controller
- **THEN** they all obtain the same instance

#### Scenario: Not initialised
- **WHEN** a controller instance is requested before the application has initialised it
- **THEN** an error is raised instead of creating an unconfigured instance

### Requirement: Replaceable injected repositories
Repositories SHALL be defined by interfaces, SHALL NOT be Singletons, and SHALL be created and configured by the application at startup and injected into the controllers that use them; controllers SHALL depend only on the repository interfaces.

#### Scenario: Alternative repository implementation
- **GIVEN** an in-memory implementation of the user repository interface
- **WHEN** the user controller is initialised with it
- **THEN** users are created and loaded through it without any change to the controller

#### Scenario: Normal and fly paths
- **WHEN** the application starts once normally and once with `--fly`
- **THEN** the repositories are configured with different directories and the same controller logic produces identical responses

### Requirement: Startup wiring
Dependencies between controllers SHALL be established during application initialisation, in dependency order, and business operations SHALL use those injected dependencies instead of constructing repositories or controllers.

#### Scenario: No construction inside operations
- **WHEN** the business and presentation packages are inspected
- **THEN** repositories and controllers are only constructed by the application's composition root
