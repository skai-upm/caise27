# Spec Delta

## Purpose

Defines how the application is structured into presentation, business and persistence layers, how abnormal situations are represented with exceptions, and how singleton controllers receive their repositories and collaborators by dependency injection.

## ADDED Requirements

### Requirement: Layered packages
The code SHALL be organised in distinct Java packages for presentation (user interaction and command processing), business (model, business rules, controllers) and persistence (repositories and file I/O) under a named root package. The default package SHALL NOT be used.

#### Scenario: Layer packages exist
- **WHEN** the source tree is inspected
- **THEN** it contains packages `es.upm.amusementpark.presentation`, `es.upm.amusementpark.business` and `es.upm.amusementpark.persistence`, and no class is in the default package

### Requirement: Layer responsibilities respected
Presentation classes SHALL NOT access persistence classes or data files directly, and domain model classes SHALL NOT store or retrieve their own persistent representation.

#### Scenario: Presentation does not depend on persistence
- **WHEN** the presentation sources are inspected
- **THEN** none of them imports anything from the persistence package or `java.nio.file.Files`

#### Scenario: Model has no file I/O
- **WHEN** the business model sources are inspected
- **THEN** none of them imports `java.io` or `java.nio.file` file APIs or persistence classes

### Requirement: Exceptions represent abnormal situations
Business operations SHALL signal abnormal situations (invalid data, missing elements, duplicates, forbidden operations, authentication failures, persistence failures) by throwing exceptions from a project exception hierarchy; presentation SHALL catch them at command level, report `ERROR: <reason>`, and continue processing the next command.

#### Scenario: Rejected operation reported and processing continues
- **WHEN** a command file contains `TICKET CREATE;T001;nadie@correo.com;2027-04-15` followed by `HELP`
- **THEN** the first line produces an `ERROR:` message and the help text is still printed

#### Scenario: Unexpected runtime failure contained
- **WHEN** an operation fails with an unexpected runtime exception
- **THEN** an `ERROR:` message is printed and the next command is processed

### Requirement: Singleton controllers
Each controller with a unique management responsibility (central park facade, system date/price, users, groups, tickets, passes, attractions, invoices) SHALL apply the Singleton pattern: a private constructor and a single shared instance.

#### Scenario: Controllers are unique
- **WHEN** any controller's instance accessor is called twice
- **THEN** the same instance is returned and the class has no public constructor

### Requirement: Repositories injected as replaceable dependencies
Repositories SHALL NOT be singletons. Repository instances SHALL be created and configured during application initialisation (with normal or `--fly` paths) and injected into the controllers that need them, together with controller collaborations, using project code only (no DI framework).

#### Scenario: Alternative repositories without changing controllers
- **WHEN** the park is initialised with in-memory test repositories instead of file repositories
- **THEN** all controller operations work unchanged and no file is written

#### Scenario: Repositories are not singletons
- **WHEN** the repository classes are inspected
- **THEN** they have public constructors and no static instance accessor
