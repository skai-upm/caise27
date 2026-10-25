# Spec Delta

## Purpose

Defines the structural rules of the code base: presentation, business and persistence layers in separate packages, single-instance controllers, and dependency injection configured at start-up so persistence locations can vary without touching business logic.

## ADDED Requirements

### Requirement: Layered packages
Production code SHALL be organised in the packages `com.amusementpark.presentation` (start-up, command interpretation and help), `com.amusementpark.business` and its subpackages (model, controllers, invoices, validation, security) and `com.amusementpark.persistence` (repositories and file access). No class SHALL be in the default package.

#### Scenario: Package layout
- **WHEN** the source tree is inspected
- **THEN** every production class belongs to one of the presentation, business or persistence packages (or their subpackages)

### Requirement: Layer responsibilities
Presentation classes SHALL NOT read or write data files; domain model classes SHALL NOT read or write files; only persistence classes SHALL access data files.

#### Scenario: Commands do not touch files
- **WHEN** the presentation and business model classes are inspected
- **THEN** none of them uses file I/O for data files (reading a command file given on the command line is the only file input in presentation)

### Requirement: Singleton controllers
Each controller representing a single management responsibility (park settings, users, groups, tickets and passes, attractions, invoices) SHALL have exactly one instance, obtained through a static accessor, with no public constructor.

#### Scenario: Single instance
- **WHEN** the user controller is requested twice
- **THEN** the same instance is returned

### Requirement: Start-up dependency injection
Repositories SHALL NOT be singletons. At start-up the application SHALL create the repositories for the selected data directory (normal or `--fly`) and inject them, together with collaborating controllers and services, into the controllers. Controllers SHALL NOT construct repositories or other controllers inside their operations.

#### Scenario: Fly mode uses other repositories without controller changes
- **WHEN** the application starts with `--fly`
- **THEN** the same controller classes operate on repositories configured for the temporary directory

### Requirement: Compatibility
The reorganisation SHALL NOT change the syntax or observable behaviour of existing E1 and E2 commands, except where later requirements of this delivery explicitly change them.

#### Scenario: Existing commands still work
- **WHEN** the E1 and E2 command scenarios are executed after the reorganisation
- **THEN** they produce the same results as before
