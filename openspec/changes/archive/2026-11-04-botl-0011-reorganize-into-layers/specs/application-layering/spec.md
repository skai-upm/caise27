# Spec Delta

## Purpose

Defines how the application's code is separated into presentation, business and persistence layers, which dependencies between them are allowed, and that the separation preserves all existing behaviour.

## ADDED Requirements

### Requirement: Layer packages
The application SHALL place its classes in distinct Java packages for presentation (command interaction), business (model, domain rules and controllers) and persistence (repositories and file access), under `es.upm.etsisi.amusementpark`. No class SHALL be in the Java default package. Only the entry point MAY stay in the root application package.

#### Scenario: Package boundaries
- **GIVEN** the reorganised source tree
- **WHEN** the Java packages are inspected
- **THEN** there are `presentation`, `business` (with sub-packages) and `persistence` packages, every production class except `App` belongs to one of them, and no source file lacks a `package` declaration

### Requirement: Presentation does not access persistence
Presentation classes SHALL NOT read or write data files, and SHALL NOT reference persistence classes. They SHALL reach stored data only through the business layer.

#### Scenario: Presentation imports
- **WHEN** the presentation sources are inspected
- **THEN** they import nothing from the `persistence` package and do not use `java.nio.file` file I/O

### Requirement: Domain model does not access persistence
Domain model classes SHALL NOT implement file storage or loading and SHALL NOT reference persistence classes or file I/O APIs.

#### Scenario: Model imports
- **WHEN** the business model sources are inspected
- **THEN** they import nothing from `persistence` and do not use `java.nio.file` file I/O

### Requirement: Behaviour preserved
All E1 and E2 commands SHALL keep their syntax, their output, and their effects on the data files after the reorganisation, and data files written before it SHALL be read unchanged.

#### Scenario: Legacy command behaviour
- **GIVEN** a valid E1/E2 command sequence and data files persisted before the refactor
- **WHEN** the same sequence is run after the refactor
- **THEN** the output lines and the resulting data files are identical to those produced before the refactor

#### Scenario: Build
- **WHEN** `mvn package` runs after the refactor
- **THEN** it succeeds and the JAR starts with `java -jar` using the same entry point
