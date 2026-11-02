# Spec Delta

## Purpose

Constrains how the application is structured so presentation, business rules, and persistence stay separated and collaborating components are wired once at startup.

## ADDED Requirements

### Requirement: Layered packages
All Java code SHALL live in named packages under `com.amusementpark.presentation` (command interaction), `com.amusementpark.business` (model, domain rules, controllers), and `com.amusementpark.persistence` (repositories and file access); the default package SHALL NOT be used.

#### Scenario: Package inspection
- **WHEN** the source tree is inspected
- **THEN** every class belongs to one of the three layer packages or their subpackages and none is in the default package

### Requirement: Layer responsibilities
Presentation classes SHALL NOT read or write persistence files, and domain model classes SHALL NOT read or write files; all file access SHALL be in the persistence layer.

#### Scenario: No file access outside persistence
- **WHEN** the presentation and business sources are inspected
- **THEN** none of them opens, reads, or writes the persistence files

#### Scenario: Behavior preserved
- **WHEN** representative E1 and E2 commands are executed after the reorganization
- **THEN** their outputs and effects are the same as before

### Requirement: Singleton controllers
Each controller representing a single management responsibility (park, users, groups, tickets, attractions, invoices) SHALL have exactly one instance per execution, obtained through its Singleton accessor.

#### Scenario: Same instance
- **WHEN** a controller's instance is requested repeatedly within one execution
- **THEN** the same object is returned each time

### Requirement: Dependency injection at startup
Repositories SHALL be ordinary, replaceable objects (not Singletons) created and configured at application startup and injected into the controllers; controller-to-controller collaborators SHALL also be injected at startup rather than created inside operations. Normal and `--fly` runs SHALL differ only in the repository configuration.

#### Scenario: Mode-specific repositories
- **WHEN** the application starts in normal mode and in `--fly` mode
- **THEN** the controllers receive repositories configured with the respective paths and execute the same business logic
