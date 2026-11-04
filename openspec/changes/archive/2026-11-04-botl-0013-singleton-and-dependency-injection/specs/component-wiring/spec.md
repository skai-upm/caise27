# Spec Delta

## Purpose

Defines how the application's unique controllers are instantiated and how repositories and collaborators are created at startup and injected into them, so controller logic is independent of the repository configuration.

## ADDED Requirements

### Requirement: Singleton controllers
Each controller with a unique management responsibility — the park controller and the user, group, ticket and attraction managers — SHALL have exactly one instance per execution. It SHALL be created only through the class's initialization operation and obtained through its single access point. Creating a second instance while one exists, or accessing it before initialization, SHALL fail.

#### Scenario: Same instance everywhere
- **GIVEN** the application has started
- **WHEN** different components obtain the user manager
- **THEN** they receive the same instance

#### Scenario: Second initialization rejected
- **GIVEN** the user manager has been initialized for the current execution
- **WHEN** another initialization is attempted
- **THEN** it fails with an error and the existing instance is unchanged

#### Scenario: New execution
- **GIVEN** an execution has ended and its controllers were reset
- **WHEN** a new execution starts
- **THEN** new controller instances are created with the new execution's repositories

### Requirement: Injected repositories
Repositories SHALL NOT be Singletons. They SHALL be created and configured with their file paths at application startup and passed to the controllers that use them; controllers SHALL NOT construct repositories.

#### Scenario: Normal and --fly configurations
- **WHEN** the application starts once normally and once with `--fly`
- **THEN** the repositories are configured with different paths while the same controller classes and logic are used, and the behaviour of every command is the same

### Requirement: Collaborations wired at startup
When a controller needs another controller or service, the dependency SHALL be provided during application initialization rather than constructed inside the operation that uses it.

#### Scenario: Invoice director injected
- **WHEN** an invoice is generated
- **THEN** the park controller uses the invoice director it received at initialization instead of creating one in the operation
