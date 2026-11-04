# application-layering Specification

## Purpose

Defines the layered package structure of the application — presentation, business and persistence — and the dependency rules that keep command interaction, domain logic and file storage separate.

## Requirements

### Requirement: Layer packages
The application SHALL organize its production classes into distinct packages for presentation (`es.upm.park.presentation`), business (`es.upm.park.business`, with domain objects in `es.upm.park.business.model`) and persistence (`es.upm.park.persistence`), and no class SHALL be in the Java default package.

#### Scenario: Package layout
- **WHEN** the production source tree is inspected
- **THEN** every class is in one of the presentation, business, business.model or persistence packages and none is in the default package

### Requirement: Allowed dependency directions
Presentation classes SHALL depend only on business classes (including the model), business classes SHALL depend on the model and on persistence, and persistence classes SHALL depend only on the model; no layer SHALL depend on presentation.

#### Scenario: Import inspection
- **WHEN** the imports of every production class are inspected
- **THEN** no presentation class imports `es.upm.park.persistence`, no business or persistence class imports `es.upm.park.presentation`, and no persistence class imports a business class outside `es.upm.park.business.model`

### Requirement: Presentation does not access persistence files
Command interaction and parsing SHALL NOT read or write persistence files or manage persistence directories; they SHALL obtain all data operations from the business layer.

#### Scenario: No file access in presentation
- **WHEN** the presentation classes are inspected
- **THEN** they contain no persistence-file reading/writing and no creation or deletion of data directories (reading the command file given as launch argument is input, not persistence)

### Requirement: Domain objects do not handle files
Domain model classes SHALL NOT read or write files; storage and loading SHALL be implemented only in the persistence layer.

#### Scenario: Model is file-free
- **WHEN** the classes in `es.upm.park.business.model` are inspected
- **THEN** none uses file input/output APIs

### Requirement: Behavior preserved after reorganization
All Delivery 1 and Delivery 2 commands SHALL keep their syntax, outputs and persisted records, and `mvn package` SHALL produce the executable JAR.

#### Scenario: Legacy command sequence
- **GIVEN** a command sequence covering users, groups, tickets, group tickets, attractions, SYSTEM DATE, SYSTEM PRICE, HELP and EXIT, and existing data files
- **WHEN** it is run with the JAR built before and after the reorganization on copies of the same data
- **THEN** the printed output and the resulting data files are identical

#### Scenario: Build
- **WHEN** `mvn package` runs
- **THEN** all tests pass and `java -jar` on the produced JAR starts the application
