# layered-packages-and-exceptions Specification

## Purpose

Organises the application into presentation, business and persistence layers in named Java packages and handles abnormal situations through exceptions.

## Requirements

### Requirement: Layered packages
The code SHALL be organised in non-default Java packages that separate presentation (user interaction and command processing), business (model, rules, controllers) and persistence (repositories and file access); the presentation layer SHALL NOT access persistence files and domain objects SHALL NOT store or retrieve their persistent representation.

#### Scenario: Package inspection
- **WHEN** the source tree is inspected
- **THEN** there is no class in the default package, file access exists only in the persistence package, and presentation classes do not import persistence classes

### Requirement: Exception-based error handling
Abnormal situations SHALL be represented and propagated as exceptions and handled at the command-processing level, so that an incorrect command or an operation that cannot be completed reports a controlled error and processing continues.

#### Scenario: Malformed operation
- **WHEN** a malformed or disallowed command is executed in a command file
- **THEN** an error is reported and the following commands are still processed

### Requirement: Behaviour preserved by the reorganisation
The reorganisation SHALL preserve the behaviour achieved at the end of Delivery 2.

#### Scenario: Delivery 2 regression
- **WHEN** the Delivery 1 and 2 command sequences are executed after the reorganisation
- **THEN** they produce the same results as before
