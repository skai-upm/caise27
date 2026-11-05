# Spec Delta

## ADDED Requirements

### Requirement: Singleton controllers
Each controller with a unique management responsibility during an execution SHALL apply the Singleton pattern: one instance, obtained through a static access point, with no accessible constructor. Persistence components SHALL NOT be singletons.

#### Scenario: Central management is unique
- **WHEN** central park management is requested from different parts of the application
- **THEN** the same instance is returned, and it has no accessible constructor

#### Scenario: Repositories are not global singletons
- **WHEN** two storages are configured for different directories
- **THEN** they are distinct instances that read and write their own files

### Requirement: Injected replaceable repositories
Repositories SHALL be created and configured during application initialization and injected into the elements that use them. Central management SHALL load and save park state only through an injected repository abstraction, without creating repositories or depending on a concrete persistence implementation. Swapping the injected repository (normal directory, `--fly` directory or any other implementation) SHALL NOT require changes to business logic. No dependency-injection framework SHALL be used.

#### Scenario: Alternative repository injected
- **GIVEN** an in-memory repository implementation injected into central management
- **WHEN** the state is saved and central management is reset, re-injected and loaded
- **THEN** the state round-trips through that implementation without touching any file

#### Scenario: Normal and fly modes differ only in configuration
- **WHEN** the application starts with and without `--fly`
- **THEN** the same repository classes are injected, configured with different directories

#### Scenario: No repository configured
- **GIVEN** central management with no repository injected
- **WHEN** state is saved or loaded
- **THEN** nothing is persisted and no error occurs
