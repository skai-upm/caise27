# Spec Delta

## ADDED Requirements

### Requirement: Single instance of each unique controller
Each controller that represents an execution-wide management responsibility (central park management, invoicing) SHALL have exactly one instance per execution, and code outside the controller SHALL NOT be able to create another instance.

#### Scenario: Controllers are unique
- **WHEN** the park management controller or the invoicing controller is requested several times from different parts of the application
- **THEN** the same instance of each is obtained every time

### Requirement: Repositories are injected dependencies
Repositories SHALL NOT be global singletons. They SHALL be created and configured during application initialization and passed to the components that use them, so that different persistence configurations can be used without changing business logic. Normal and `--fly` executions SHALL differ only in the repository configuration (paths).

#### Scenario: Two independent configurations
- **GIVEN** two data directories
- **WHEN** the application configuration builds persistence for each of them
- **THEN** distinct repository instances are created, each reading and writing only its own directory

#### Scenario: Fly mode uses the same repository classes
- **WHEN** the application starts with `--fly`
- **THEN** the same repository classes are configured with the temporary directory and the business logic is unchanged

### Requirement: Collaborations configured at initialization
When a controller or presentation component needs another controller, the relationship SHALL be established during application initialization by injection, not by creating the dependency inside each operation. Dependency injection SHALL be implemented with project code, without injection frameworks.

#### Scenario: Invoicing collaborates with park management
- **GIVEN** the application has been initialized
- **WHEN** an invoice is generated
- **THEN** the invoicing controller uses the park management controller injected at initialization

#### Scenario: Unconfigured collaboration is reported
- **GIVEN** the invoicing controller has not been given its collaborator
- **WHEN** an invoice is requested
- **THEN** the request fails with an error that states the missing configuration instead of creating the collaborator itself
