# Spec Delta

## ADDED Requirements

### Requirement: Single-instance controllers
Controllers that represent a single management responsibility (the park controller and the user, group, ticket and attraction managers) SHALL have exactly one instance in the application, obtained through a class-level accessor; they SHALL NOT be instantiable from other classes.

#### Scenario: Same instance
- **WHEN** any component obtains the user manager twice
- **THEN** both references denote the same instance

#### Scenario: No public construction
- **WHEN** a class outside a controller tries to create it with `new`
- **THEN** the code does not compile because the constructor is private

### Requirement: Repositories injected at startup
Persistence repositories SHALL be defined by abstractions that controllers depend on, SHALL NOT be singletons, and SHALL be created and configured at startup for the selected data location and injected into the controllers; controllers SHALL NOT create repositories.

#### Scenario: Normal and fly modes
- **WHEN** the application starts normally and with `--fly`
- **THEN** the same controller logic runs with repositories configured for `data/` or for the temporary directory respectively

#### Scenario: Alternative implementation
- **WHEN** a test injects an in-memory implementation of a repository abstraction
- **THEN** the controller works with it without code changes

### Requirement: Collaborations configured at initialization
When a controller collaborates with another controller or service, the dependency SHALL be injected during application initialization and SHALL NOT be constructed inside the operation that uses it.

#### Scenario: Invoice director injected
- **WHEN** an invoice is generated
- **THEN** the park controller uses the invoice director injected at startup rather than creating a new one
