# layered-architecture Specification

## Purpose

Defines the physical layered structure of the application — separate presentation, business and persistence packages and the dependencies allowed between them — so responsibilities stay separated as the system grows.

## Requirements

### Requirement: Layer packages
The code SHALL be organised into at least three layer packages: a presentation layer handling command-based interaction, a business layer containing the domain model, domain rules and controllers, and a persistence layer containing repositories and file access. Subpackages MAY be used inside a layer. No class SHALL be in the default package.

#### Scenario: Packages present
- **WHEN** the source tree is inspected
- **THEN** it contains `es.upm.parque.presentation`, `es.upm.parque.business` and `es.upm.parque.persistence`, and every class declares a package

### Requirement: Presentation does not access persistence
Presentation classes (command handling) SHALL interact only with the business layer and SHALL NOT use repositories or read or write persistence files directly.

#### Scenario: Command processor dependencies
- **WHEN** the presentation package's classes are inspected
- **THEN** none of them references the persistence package or file-access APIs

### Requirement: Domain objects do not access files
Domain model classes SHALL NOT read or write files or depend on the persistence layer; storing and loading them is the responsibility of the persistence layer.

#### Scenario: Model dependencies
- **WHEN** the business model package's classes are inspected
- **THEN** none of them references the persistence package or file-access APIs

### Requirement: Behaviour preserved
The reorganisation SHALL NOT change the syntax, outputs or effects of any existing command, the execution modes, or the data file formats, and the project SHALL still build with `mvn package` into the executable `target/parque.jar`.

#### Scenario: Existing commands
- **WHEN** the Delivery 1 and Delivery 2 command sequences are run after the reorganisation
- **THEN** they produce the same responses and stored data as before
