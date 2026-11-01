# layered-architecture Specification

## Purpose

Keeps presentation, business, and persistence responsibilities physically separated in distinct Java packages so each layer can evolve without leaking concerns into the others.

## Requirements

### Requirement: Layered package structure
All application code SHALL live in named packages under a `presentation` layer (command interaction and application start-up), a `business` layer (domain model, domain rules, controllers), and a `persistence` layer (repositories and file access), with subpackages where useful. No class SHALL be in the default package.

#### Scenario: Packages exist
- **WHEN** the source tree is inspected
- **THEN** `presentation`, `business`, and `persistence` packages exist and every class belongs to one of them

### Requirement: Layer responsibility boundaries
Command handling SHALL NOT read or write persistence files directly, and domain objects SHALL NOT read or write files; all file reading and writing SHALL be performed by persistence-layer classes.

#### Scenario: File access confined to persistence
- **WHEN** the presentation and business sources are inspected
- **THEN** none of them opens, reads, or writes a data file

### Requirement: Behaviour preserved by the reorganisation
All E1 and E2 commands SHALL keep their syntax and observable behaviour, and `mvn package` SHALL build the executable JAR.

#### Scenario: Regression after reorganisation
- **GIVEN** the E1/E2 automated tests
- **WHEN** they run against the reorganised code
- **THEN** they pass and `mvn package` produces `target/parque.jar`
