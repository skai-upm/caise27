# Spec Delta

## ADDED Requirements

### Requirement: Singleton controllers
Each controller whose responsibility is a unique management function during an execution SHALL apply the Singleton pattern: it SHALL have no public constructor and SHALL be obtained through a single access point that always returns the same instance. Persistence components SHALL NOT be singletons.

#### Scenario: Unique controllers
- **WHEN** central park management or invoice control is requested several times
- **THEN** the same instance is returned each time and no public constructor exists

#### Scenario: Repositories are not singletons
- **WHEN** repositories are created for two different directories
- **THEN** two independent repository sets exist, each using its own directory

### Requirement: Injected replaceable repositories
Repository instances SHALL be created and configured during application initialization and injected into the controllers that need them; controllers SHALL depend only on repository abstractions defined in the business layer, so that normal and `--fly` executions use the same repository implementations configured with different paths without changing controller logic. Dependency injection SHALL be implemented with project code, without frameworks.

#### Scenario: Same controller, different repository configuration
- **WHEN** the controller is configured once with repositories for directory A and once with repositories for directory B
- **THEN** its state is loaded from and saved to the configured directory only, using unchanged business logic

#### Scenario: Replaceable implementation
- **WHEN** an in-memory implementation of the repository abstractions is injected instead of the text-file one
- **THEN** the controller saves and reloads its state through it

### Requirement: Injected collaborations
When a controller collaborates with another controller or service, the collaboration SHALL be established during application initialization rather than by creating the collaborator inside each operation.

#### Scenario: Invoice control collaborators
- **WHEN** an invoice is requested
- **THEN** it is produced by the invoice controller using the central park management and invoice director instances injected at initialization, without creating a new director in the operation
