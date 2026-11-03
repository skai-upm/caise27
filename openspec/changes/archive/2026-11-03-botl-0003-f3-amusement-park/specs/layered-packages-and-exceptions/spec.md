# Spec Delta

## Purpose

Organizes the application into presentation, business and persistence packages and makes exceptions the mechanism for propagating abnormal situations to a level that handles them in a controlled way (source F3-C01, sections 3 and 4).

## ADDED Requirements

### Requirement: Layered packages
The code SHALL be organized in non-default Java packages that separate presentation (user interaction and command processing), business (model, business rules and control elements) and persistence (repositories and reading/writing persistent information).

#### Scenario: Package responsibilities
- **WHEN** the source tree is inspected
- **THEN** every class is in a named package, command processing is in the presentation package, model and controllers are in the business package, and repositories are in the persistence package

### Requirement: Layer access rules
The presentation layer SHALL NOT access persistence files directly, and domain objects SHALL NOT store or retrieve their own persistent representations.

#### Scenario: No file access outside persistence
- **WHEN** presentation and business classes are inspected
- **THEN** none of them reads or writes the park data files; only persistence classes do

### Requirement: Exception-based error handling
Abnormal situations in operations SHALL be represented and propagated with exceptions and handled at the presentation level, so an incorrect command or an operation that cannot be completed reports an error and command processing continues.

#### Scenario: Failed operation does not stop processing
- **WHEN** a malformed or disallowed command is followed by a valid command
- **THEN** the first reports a controlled error and the second is executed normally
