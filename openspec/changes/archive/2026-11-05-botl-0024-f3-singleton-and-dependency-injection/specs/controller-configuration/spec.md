# Spec Delta

## Purpose

Defines how the controllers of the park are instantiated and wired: controllers with a unique management responsibility are singletons, repositories are replaceable dependencies created and injected during application initialization, and collaborations between controllers are configured at that moment with project code, without dependency injection frameworks.

## ADDED Requirements

### Requirement: Controllers with a unique responsibility are singletons
`ParkController` and the business managers (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `SystemManager` and `InvoiceManager`) SHALL each apply the Singleton pattern explicitly: they SHALL NOT expose a public constructor and SHALL provide a `getInstance()` operation that always returns the same instance during an execution.

#### Scenario: Repeated access returns the same controller
- **WHEN** `getInstance()` is called twice on any of these controllers
- **THEN** both calls return the same object

#### Scenario: Controllers cannot be instantiated from outside
- **WHEN** the declared constructors of these controllers are inspected
- **THEN** none of them is public

### Requirement: Repositories are replaceable dependencies injected at initialization
Repositories SHALL NOT be singletons. The user, group, ticket and attraction repositories, together with the storage they use, SHALL be created and configured during application initialization (normal mode with the data files of the working directory, `--fly` mode with a temporary directory) and injected into `ParkController`; controllers SHALL use the injected repositories and SHALL NOT create repositories themselves, neither at start-up nor in any operation. The same controller code SHALL work with any injected repository configuration.

#### Scenario: Controllers use the injected repositories
- **GIVEN** two different repository configurations A and B
- **WHEN** the application is initialized with A, a user is created, and then the application is initialized with B
- **THEN** the user is stored in A's user repository
- **AND** after initializing with B, the controllers see B's repositories and the user is not present

#### Scenario: Normal and fly modes differ only in configuration
- **GIVEN** a normal execution with data files in the working directory and a `--fly` execution with a temporary directory
- **WHEN** the same commands are executed in both
- **THEN** both produce the same responses, and only the configured storage location differs

#### Scenario: Repositories are not global singletons
- **WHEN** the repository classes are inspected
- **THEN** they expose public constructors and no `getInstance()` operation

### Requirement: Controller collaborations are configured at initialization
When a controller needs another controller, the collaborator SHALL be injected during initialization rather than created within the controller or within an operation; presentation command handlers SHALL receive the configured controllers rather than instantiating them. Using a controller before the application has been initialized SHALL fail with a clear `IllegalStateException`.

#### Scenario: Attraction management uses the configured ticket management
- **WHEN** the application is initialized
- **THEN** `AttractionManager` uses the single `TicketManager` instance to resolve tickets, and no manager source creates another manager with `new`

#### Scenario: Use before initialization
- **GIVEN** a manager whose dependencies have not been injected
- **WHEN** one of its operations is invoked
- **THEN** an `IllegalStateException` explains that the application has not been initialized

### Requirement: Dependency injection without frameworks
Dependency injection SHALL be implemented with Java code developed in the project; the build SHALL NOT declare any dependency injection framework (such as Spring, Guice, Dagger, CDI or PicoContainer).

#### Scenario: No DI framework in the build
- **WHEN** the project build file is inspected
- **THEN** it declares no dependency injection framework
