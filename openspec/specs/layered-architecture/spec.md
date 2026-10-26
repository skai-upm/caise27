# layered-architecture Specification

## Purpose

Defines how the application's code is organized into presentation, business and persistence layers and which responsibilities each layer may hold.

## Requirements

### Requirement: Layer packages
The code SHALL be organized into separate Java packages for the presentation, business and persistence layers (with subpackages where useful), and no class SHALL be in the default package.

#### Scenario: Package structure
- **WHEN** the source tree is inspected
- **THEN** it contains `es.upm.parque.presentation`, `es.upm.parque.business` (with its subpackages) and `es.upm.parque.persistence`, and every class declares a package

### Requirement: Presentation does not access files
Classes of the presentation layer SHALL interact only with business-layer controllers and model objects and SHALL NOT read or write persistence files or use repositories directly.

#### Scenario: Commands go through controllers
- **WHEN** the presentation package is inspected
- **THEN** it has no reference to repository classes or to file reading/writing APIs for data files

### Requirement: Domain objects do not access files
Business model classes SHALL NOT read or write files; storing and loading data SHALL be done only by the persistence layer.

#### Scenario: Model is persistence-free
- **WHEN** the business model package is inspected
- **THEN** it has no reference to repositories or file I/O APIs

### Requirement: Behavior preserved
The reorganization SHALL NOT change the syntax or results of any existing command, the execution modes or the persisted file formats, and `mvn package` SHALL still produce the executable JAR.

#### Scenario: Same session, same output
- **WHEN** a command file exercising Delivery 1 and 2 commands is run before and after the reorganization
- **THEN** both runs print identical output

### Requirement: Unique controllers
Each business controller representing a single management responsibility (users, groups, tickets, attractions, invoices) SHALL have exactly one instance in the application, obtained through a single access point; it SHALL NOT be instantiable from other classes.

#### Scenario: Same instance everywhere
- **WHEN** the user controller is requested from two different places
- **THEN** both receive the same instance

### Requirement: Dependencies injected at startup
Repositories and every collaborator a controller needs (other controllers, the system date, the invoice builder) SHALL be created or obtained during application initialization and injected into the controller; controllers SHALL NOT construct their dependencies themselves. Repositories SHALL NOT be singletons.

#### Scenario: Fly mode without controller changes
- **WHEN** the application starts with `--fly`
- **THEN** the same controllers are configured with repositories pointing to the temporary directory and behave exactly as in normal mode

#### Scenario: Unconfigured controller
- **WHEN** a controller is used before being configured
- **THEN** the operation fails with an error stating the controller is not configured
