## ADDED Requirements

### Requirement: Singleton controllers
Each controller with a unique management responsibility during an execution (the park controller and the invoice controller) SHALL apply the Singleton pattern: one instance per execution, private construction and a single access point.

#### Scenario: Unique controller instances
- **WHEN** the park controller or the invoice controller is obtained several times from different places
- **THEN** the same instance is returned each time and no other instance can be constructed

### Requirement: Injected replaceable repositories
Repositories SHALL NOT be singletons. Their instances SHALL be created and configured (data or temporary paths) during application initialization and injected into the controllers that use them; controllers SHALL depend on repository abstractions so that a different persistence implementation or configuration can be used without changing controller business logic.

#### Scenario: Alternative repository implementation
- **WHEN** the park controller is configured with in-memory repository implementations instead of the text-file ones
- **THEN** it loads and saves the park state through them without any change to its business logic

#### Scenario: Normal and fly modes differ only in configuration
- **WHEN** the application starts in normal mode and in `--fly` mode
- **THEN** both inject the same text-file repository classes, configured with different directories

### Requirement: Injected controller collaborations
Collaborations between controllers and with the presentation layer SHALL be established during initialization by injection, not by creating or looking up dependencies inside each operation. Dependency injection SHALL be implemented with project code, without DI frameworks.

#### Scenario: Invoice controller collaborates with the park controller
- **WHEN** the application initializes
- **THEN** the invoice controller receives the park controller it collaborates with, and the command processor receives both controllers
