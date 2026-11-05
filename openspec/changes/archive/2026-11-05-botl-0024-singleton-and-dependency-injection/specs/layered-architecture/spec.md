## ADDED Requirements

### Requirement: Singleton controllers
Each controller responsible for a unique management function during an execution (user, group, ticket, attraction and invoice management, and the central park manager) SHALL apply the Singleton pattern explicitly: it MUST NOT be instantiable from outside and every request SHALL obtain the same instance.

#### Scenario: One instance per controller
- **WHEN** a controller is requested twice from different parts of the application
- **THEN** the same instance is returned and the controller class has no accessible constructor

### Requirement: Repositories injected as replaceable dependencies
Repositories SHALL NOT be global singletons: they SHALL be created and configured during application initialization (file-backed in the configured data directory, or any other implementation/configuration) and injected into the park manager and the controllers, which MUST NOT create repositories themselves. Collaborations between controllers SHALL also be established during initialization. Dependency injection SHALL be implemented with project code, without frameworks.

#### Scenario: Different repository configurations without changing controllers
- **GIVEN** one initialization with file repositories in directory A and another with repositories in directory B (as in normal and `--fly` mode)
- **WHEN** the same commands are processed after each initialization
- **THEN** the controllers operate on the injected repositories of each configuration, and data is written only to the configured directory

#### Scenario: Injected repositories are used, not recreated
- **GIVEN** an initialization with a given set of repositories
- **WHEN** a user is created through the commands
- **THEN** the user is stored in exactly the injected users repository
