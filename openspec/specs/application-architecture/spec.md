# application-architecture Specification

## Purpose
Defines the structural organisation of the code base into presentation, business and persistence layers and the responsibilities each layer may take.

## Requirements

### Requirement: Layer packages
The code SHALL be organised in separate packages for the presentation layer (`es.upm.parque.presentation`), the business layer (`es.upm.parque.business` with subpackages for model, controllers and validation) and the persistence layer (`es.upm.parque.persistence`). No class SHALL be in the default package.

#### Scenario: Every class in a layer
- **WHEN** the source tree is inspected
- **THEN** every class declares a package under `es.upm.parque.presentation`, `es.upm.parque.business` or `es.upm.parque.persistence`

### Requirement: Commands do not access persistence
Command interpretation SHALL NOT read or write persistence files nor use repositories directly; it SHALL delegate to business controllers.

#### Scenario: Command processor dependencies
- **WHEN** the command-processing class is inspected
- **THEN** it imports no persistence-layer class and no file I/O API

### Requirement: Domain objects do not handle files
Domain model classes SHALL NOT read or write files; file access SHALL be confined to the persistence layer and the input handling of the presentation layer.

#### Scenario: Model independence
- **WHEN** the business model package is inspected
- **THEN** none of its classes uses file I/O APIs

### Requirement: Behaviour preserved
The reorganisation SHALL NOT change the syntax, responses or effects of any existing command, the persisted file formats, or the execution modes, and the project SHALL still build with `mvn package`.

#### Scenario: Existing commands still work
- **WHEN** the full Delivery 1 and 2 test suite is run after the reorganisation
- **THEN** all tests pass and `mvn package` produces `target/parque.jar`
