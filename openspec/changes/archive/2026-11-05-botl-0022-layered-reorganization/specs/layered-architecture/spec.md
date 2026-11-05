# Spec Delta

## Purpose

Defines how the application code is organised into presentation, business and persistence layers and how abnormal situations are represented with exceptions and handled at the appropriate layer.

## ADDED Requirements

### Requirement: Layered packages
The source code SHALL be organised under the root package `es.upm.amusementpark` (no class in the default package) into a `presentation` package (user interaction and command processing), a `business` package (domain model, business rules and application control, including the central park manager) and a `persistence` package (repositories and reading/writing of persistent data); subpackages are allowed.

#### Scenario: Every class belongs to a layer
- **WHEN** the production classes are listed
- **THEN** each one is in `es.upm.amusementpark.presentation`, `es.upm.amusementpark.business` or `es.upm.amusementpark.persistence` (or a subpackage), and none is in the default package

### Requirement: Layer dependency rules
The presentation layer MUST NOT read or write persistent data files directly, and domain model classes MUST NOT store or retrieve their own persistent representations; the business model MUST NOT depend on the presentation layer.

#### Scenario: Presentation does not touch data files
- **WHEN** the presentation classes are inspected
- **THEN** none of them reads or writes the users, groups, tickets or attractions files (they only configure the data directory and read command input)

#### Scenario: Model independent of persistence and presentation
- **WHEN** the classes of the business model package are inspected
- **THEN** they import nothing from the persistence or presentation packages

### Requirement: Exceptions represent abnormal situations
Business operations SHALL signal abnormal situations by throwing exceptions from a defined hierarchy — not found elements, invalid data and violated business rules as specialisations of a common operation exception, and storage failures as a persistence exception — and the presentation layer SHALL handle them so that an incorrect command or an operation that cannot be completed never terminates command processing.

#### Scenario: Rejected operation reported and processing continues
- **WHEN** `TICKET DELETE;NOPE` followed by `HELP` is processed
- **THEN** an error message starting with `ERROR` is printed for the first command and the help text for the second

#### Scenario: Exception categories
- **WHEN** a non-existent group is referenced, an invalid DNI is supplied, or a locked group is modified
- **THEN** the business layer throws respectively a not-found, an invalid-data and a rule-violation exception, all handled as rejected operations
