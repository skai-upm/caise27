# application-layering Specification

## Purpose

Defines how the application is physically organised into presentation, business and persistence layers, and which dependencies between them are allowed.

## Requirements

### Requirement: Layered packages
The application SHALL place its classes in distinct Java packages for presentation (command interaction), business (domain model, domain rules and controllers) and persistence (repositories and file access), with subpackages where needed, and SHALL NOT use the default package.

#### Scenario: Package structure
- **GIVEN** the source tree
- **WHEN** its packages are inspected
- **THEN** `es.upm.etsisi.amusementpark.presentation`, `es.upm.etsisi.amusementpark.business` (with `model` and `controller`) and `es.upm.etsisi.amusementpark.persistence` exist and no `.java` file lacks a package declaration

### Requirement: Presentation does not access files
Presentation classes SHALL NOT read or write persistence files nor reference persistence classes; they SHALL interact with the business layer only.

#### Scenario: No persistence imports in presentation
- **WHEN** the presentation package is inspected
- **THEN** none of its classes imports `es.upm.etsisi.amusementpark.persistence` or uses file I/O on data files

### Requirement: Domain objects do not persist themselves
Domain model classes SHALL NOT perform file storage or loading, nor depend on the persistence or presentation layers.

#### Scenario: Model independence
- **WHEN** the `business.model` package is inspected
- **THEN** none of its classes imports persistence, presentation or `java.nio.file`/`java.io` file APIs

### Requirement: Behaviour preserved
After the reorganisation every Delivery 1 and Delivery 2 command SHALL accept the same input and produce the same responses and persisted data as before, and `mvn package` SHALL still produce the executable JAR.

#### Scenario: Legacy command sequence
- **GIVEN** a command file exercising users, groups, tickets, group tickets, attractions, system date and price, and existing data files
- **WHEN** it is run with the JAR built before and after the reorganisation on copies of the same data
- **THEN** the outputs and the resulting data files are identical

#### Scenario: Build
- **WHEN** `mvn package` runs
- **THEN** the executable JAR is produced and starts with `java -jar`
