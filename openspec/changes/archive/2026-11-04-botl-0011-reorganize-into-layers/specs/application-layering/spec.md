# Spec Delta

## Purpose

Defines the layered architecture of the application — presentation, business and persistence packages with fixed dependency directions — and guarantees that the reorganisation preserves all existing behaviour.

## ADDED Requirements

### Requirement: Layered packages
Production code SHALL be organised in the packages `es.upm.park.presentation` (command-based interaction), `es.upm.park.business` with subpackages `model` (domain objects) and `service` (domain rules and controllers), and `es.upm.park.persistence` (repositories and file access). No production class SHALL be in the default package or directly in `es.upm.park`.

#### Scenario: Package inspection
- **WHEN** the source tree under `src/main/java` is inspected
- **THEN** every class belongs to one of the presentation, business.model, business.service or persistence packages, and none is in the default package

### Requirement: Dependency directions
Presentation classes SHALL depend only on business classes and the Java standard library, and SHALL NOT import persistence classes or read or write persistence files. Business model classes SHALL NOT depend on services, presentation or persistence. Persistence classes SHALL NOT depend on presentation or business services.

#### Scenario: Presentation does not touch persistence
- **WHEN** the imports of the presentation classes are inspected
- **THEN** none refers to `es.upm.park.persistence`

#### Scenario: Domain objects do not handle files
- **WHEN** the business model classes are inspected
- **THEN** none imports `java.nio.file`, `java.io` file classes, persistence or service packages

### Requirement: Behaviour preserved
All E1 and E2 commands SHALL keep their syntax, replies and persisted effects, existing data files SHALL remain readable, and `mvn package` SHALL produce the executable JAR.

#### Scenario: Legacy command sequence
- **GIVEN** data files written before the reorganisation
- **WHEN** the same E1/E2 command sequence is run after it
- **THEN** the printed results and persisted records are equivalent, and the full pre-existing test suite passes

#### Scenario: Build
- **WHEN** `mvn package` runs
- **THEN** the executable JAR is generated and `java -jar` starts the application
