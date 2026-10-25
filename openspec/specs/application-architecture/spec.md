# application-architecture Specification

## Purpose

Defines the structural constraints of the application: layered packages, single controller instances and repositories configured at startup and injected.

## Requirements

### Requirement: Layered packages
Source code SHALL be organised into separate packages for presentation (command interaction), business (model, domain rules, controllers) and persistence (repositories and file access), with subpackages where useful. No class SHALL be in the default package.

#### Scenario: Package structure
- **WHEN** the main source tree is inspected
- **THEN** every class declares a package, and presentation, business and persistence packages exist

### Requirement: Layer dependencies
Presentation code SHALL NOT access persistence classes or files directly; business code SHALL NOT depend on presentation or persistence implementations; domain model classes SHALL NOT read or write files.

#### Scenario: Commands do not touch persistence
- **WHEN** the imports of presentation classes are inspected
- **THEN** none refers to the persistence package or to file I/O APIs

#### Scenario: Domain objects do not do I/O
- **WHEN** the imports of model classes are inspected
- **THEN** none refers to file reading or writing APIs or to the persistence package

### Requirement: Behaviour preserved
The reorganisation SHALL NOT change the syntax or behaviour of any E1 or E2 command, nor the interactive, file and `--fly` execution modes.

#### Scenario: Previous commands still work
- **WHEN** the E1 and E2 command scenarios are executed after the reorganisation
- **THEN** they produce the same results as before

### Requirement: Singleton controllers
Each controller that represents a single management responsibility SHALL have exactly one instance, obtained through a Singleton access point.

#### Scenario: Same instance
- **WHEN** a controller's instance is requested twice
- **THEN** the same object is returned and no other instance can be created from outside the class

### Requirement: Injected repositories
Repositories SHALL NOT be Singletons. They SHALL be created and configured at startup and injected into the controllers that use them, together with the controller-to-controller dependencies, so normal and `--fly` executions differ only in the repository paths.

#### Scenario: Fly uses other paths with the same controllers
- **WHEN** the application starts in `--fly` mode
- **THEN** the same controller classes are configured with repositories pointing at the temporary directory and behave as in normal mode
