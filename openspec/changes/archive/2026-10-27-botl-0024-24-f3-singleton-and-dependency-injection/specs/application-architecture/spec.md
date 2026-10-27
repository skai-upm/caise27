# Spec Delta

## ADDED Requirements

### Requirement: Singleton controllers
The controllers responsible for a unique management function during an execution — the park controller and the user, group, ticket, attraction and invoice controllers — SHALL each have exactly one instance, guaranteed by the Singleton pattern regardless of where they are requested.

#### Scenario: One instance per controller
- **WHEN** any of these controllers is requested several times from different parts of the application
- **THEN** the same instance is returned and the class offers no public constructor

### Requirement: Injected replaceable repositories
Repositories SHALL NOT be singletons. Repository instances SHALL be created and configured during application initialization and injected into the controllers that use them; collaborations between controllers SHALL also be established at initialization. Dependency injection SHALL be implemented with project code, without frameworks.

#### Scenario: Different configurations without changing business logic
- **WHEN** the application is initialized once with repositories stored in `data/` and once with repositories stored in a `--fly` temporary directory
- **THEN** the same controllers operate on the injected repositories of each configuration, with no change to their business logic

#### Scenario: In-memory repositories injected for testing
- **WHEN** the controllers are initialized with in-memory repositories
- **THEN** operations work on those repositories and no files are written

#### Scenario: Repositories are not global singletons
- **WHEN** the repository classes are inspected
- **THEN** they have public constructors and no static instance accessor
