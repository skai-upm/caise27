# layered-application Specification

## Purpose

Separates the application into presentation, business and persistence layers with clear responsibility boundaries, without changing command behavior.

## Requirements

### Requirement: Layered packages
All Java classes SHALL reside in named packages organized into a presentation layer (command interaction), a business layer (domain model, rules and controllers) and a persistence layer (repositories and file access); the default package SHALL NOT be used.

#### Scenario: Layer packages exist
- **WHEN** the source tree is inspected
- **THEN** it contains `presentation`, `business` and `persistence` packages and no class in the default package

### Requirement: Presentation does not access files
Command handling in the presentation layer SHALL NOT read or write persistence files; it SHALL act only through business-layer controllers.

#### Scenario: Commands go through controllers
- **WHEN** the presentation classes are inspected
- **THEN** none of them reads or writes the data files

### Requirement: Domain objects do not perform persistence
Domain model classes SHALL NOT read or write files; file access SHALL be confined to persistence-layer repositories.

#### Scenario: Persistence confined
- **WHEN** the business model classes are inspected
- **THEN** none of them performs file input or output

### Requirement: Behavior preserved by reorganization
All E1 and E2 command syntaxes and observable results SHALL remain unchanged after the reorganization, and `mvn package` SHALL build the executable JAR.

#### Scenario: E1/E2 regression
- **GIVEN** a representative sequence of E1 and E2 commands
- **WHEN** it is executed after the reorganization
- **THEN** the replies and persisted state are the same as before
