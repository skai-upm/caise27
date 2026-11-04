# Spec Delta

## Purpose

Defines how the application's code is organised into presentation, business and persistence layers, what each layer is responsible for, and which dependencies between them are allowed.

## ADDED Requirements

### Requirement: Layered packages
The application SHALL organise its production classes into distinct Java packages for presentation (`com.parque.presentation`), business (`com.parque.business` with subpackages for model, services and controller) and persistence (`com.parque.persistence`); no class SHALL be in the default package, and only the bootstrap class `App` MAY reside directly in `com.parque`.

#### Scenario: Package structure
- **WHEN** the production source tree is inspected
- **THEN** every class belongs to one of `com.parque.presentation`, `com.parque.business.model`, `com.parque.business.service`, `com.parque.business.controller`, `com.parque.persistence`, or is `com.parque.App`, and no source file lacks a package declaration

### Requirement: Presentation does not access persistence files
Presentation classes SHALL handle command-based interaction only (reading commands, parsing them, formatting responses) and SHALL NOT read or write data files nor depend on persistence classes.

#### Scenario: No persistence imports in presentation
- **WHEN** the imports and referenced types of `com.parque.presentation` classes are inspected
- **THEN** none refers to `com.parque.persistence` classes, and none uses file-writing APIs on data files

### Requirement: Domain objects do not handle files
Domain model classes SHALL NOT read or write files nor depend on persistence or presentation classes.

#### Scenario: Pure model package
- **WHEN** the imports of `com.parque.business.model` classes are inspected
- **THEN** none refers to `java.nio.file`, `java.io` file classes, `com.parque.persistence` or `com.parque.presentation`

### Requirement: Business independent of presentation
Business classes SHALL NOT depend on presentation classes; the main controller holding the system date and ticket price SHALL be in the business layer.

#### Scenario: Controller in business
- **WHEN** the business packages are inspected
- **THEN** the class holding the current system date and ticket price is in `com.parque.business.controller`, and no business class imports `com.parque.presentation`

### Requirement: Behaviour preserved
All E1 and E2 commands SHALL keep their syntax, responses and persisted effects after the reorganisation, and `mvn package` SHALL still produce the executable JAR.

#### Scenario: Same script, same results
- **GIVEN** a command script exercising users, groups, system date and price, tickets, group tickets, attractions, HELP and EXIT
- **WHEN** it is run with the application before and after the reorganisation on empty data directories
- **THEN** the printed output and the resulting data files are identical

#### Scenario: Build
- **WHEN** `mvn package` is run
- **THEN** the build succeeds and `java -jar` runs the application
