# Spec Delta

## Purpose

Defines the structural constraints of the park application: separate presentation, business and persistence packages, unique controllers, and dependencies configured at startup and injected.

## ADDED Requirements

### Requirement: Layered packages
Source code SHALL be organized in the packages `es.upm.amusementpark.presentation` (command interaction and application start), `es.upm.amusementpark.business` (model, rules, controllers) and `es.upm.amusementpark.persistence` (repositories and file access), optionally with subpackages. No class SHALL be in the default package. Presentation SHALL NOT access persistence files and domain objects SHALL NOT read or write files.

#### Scenario: Package boundaries
- **WHEN** the source tree is inspected
- **THEN** every class belongs to one of the three layers, no business or presentation class performs file I/O on data files, and no class is in the default package

#### Scenario: Behavior preserved
- **WHEN** the E1/E2 command scenarios are executed after the reorganization
- **THEN** they produce the same results as before

### Requirement: Singleton controllers
Controllers that represent a unique management responsibility (park, users, groups, tickets, passes, attractions, invoices) SHALL expose a single instance per execution through the Singleton pattern. Repositories SHALL NOT be Singletons.

#### Scenario: Same controller instance
- **WHEN** a controller instance is requested twice during one execution
- **THEN** the same instance is returned

### Requirement: Startup dependency injection
Repositories SHALL be created at startup from the active storage location (normal or `--fly`) and injected into the controllers; dependencies among controllers and the help formatters SHALL also be injected at startup rather than created inside the operations that use them. Controller logic SHALL be identical in normal and `--fly` modes.

#### Scenario: Repositories configured per mode
- **WHEN** the application starts in normal mode and in `--fly` mode
- **THEN** the controllers receive repositories pointing to the working directory and to the temporary directory respectively, with no mode-specific controller code
