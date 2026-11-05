# layered-architecture Specification

## Purpose
Defines how the application code is organized into presentation, business and persistence layers, and the dependency rules that keep their responsibilities separate.

## Requirements

### Requirement: Layered packages
The production code SHALL be organized in named packages under `es.upm.amusementpark`, with distinct packages for presentation (user interaction and command processing), business (model, business rules and application control) and persistence (repositories and reading and writing persistent data). No source file SHALL be in the default package.

#### Scenario: Layers present
- **GIVEN** the production sources
- **WHEN** their packages are inspected
- **THEN** command processing and handlers are in `es.upm.amusementpark.presentation`, domain and control classes are in `es.upm.amusementpark.business`, and repositories are in `es.upm.amusementpark.persistence`

#### Scenario: No default package
- **WHEN** every production source file is inspected
- **THEN** each one declares a package

### Requirement: Layer dependency rules
The presentation layer SHALL NOT access persistence files or classes directly. The business layer SHALL depend on neither presentation nor persistence, and domain objects SHALL NOT store or retrieve their own persistent representation. Wiring of the layers SHALL happen only in the application entry point.

#### Scenario: Presentation does not use persistence
- **WHEN** presentation sources are inspected
- **THEN** none import `es.upm.amusementpark.persistence` or file I/O APIs

#### Scenario: Business is independent
- **WHEN** business sources are inspected
- **THEN** none import presentation or persistence packages or file I/O APIs

### Requirement: Behavior preserved by the reorganization
Reorganizing the layers SHALL NOT change any observable behavior of the Delivery 1 and Delivery 2 commands, their messages, or persistence.

#### Scenario: Delivery 2 flow unchanged
- **GIVEN** the Delivery 2 integration flow
- **WHEN** it runs after the reorganization
- **THEN** it produces the same messages and persisted state as before

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
