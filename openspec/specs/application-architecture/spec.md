# application-architecture Specification

## Purpose

Fixes the layered organisation of the application so presentation, business and persistence responsibilities stay physically separated as the system grows.

## Requirements

### Requirement: Layered packages
The source code SHALL be organised in the packages `es.upm.parque.presentation`, `es.upm.parque.business` (with subpackages for the model and for controllers) and `es.upm.parque.persistence`, and SHALL NOT use the default package. The presentation layer SHALL contain the command-based interaction; the business layer the model, domain rules and controllers; the persistence layer the repositories and file access.

#### Scenario: Package inspection
- **WHEN** the source tree under `src/main/java` is inspected
- **THEN** every class is in one of the presentation, business (model or controller) or persistence packages and none is in the default package

### Requirement: Layer access rules
Command handling in the presentation layer SHALL NOT access persistence repositories or files directly; it SHALL act only through business controllers. Domain model objects SHALL NOT read or write files. Persistence classes SHALL only convert between files and domain objects.

#### Scenario: Commands go through the business layer
- **WHEN** the command-processing class is inspected
- **THEN** it references no persistence class and no file API, only business controllers and model types

#### Scenario: Model free of I/O
- **WHEN** the classes of the model package are inspected
- **THEN** none of them uses file or stream I/O APIs

### Requirement: Behavior preserved
The reorganisation SHALL NOT change the syntax, responses or effects of any existing command, the data file formats, or the `--fly` mode, and the project SHALL build with `mvn package`.

#### Scenario: Existing commands
- **WHEN** the E1 and E2 command sequences used by the existing tests are executed after the reorganisation
- **THEN** they produce the same responses and stored files as before

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
