# Spec Delta

## Purpose

Separates presentation, business and persistence responsibilities into distinct Java packages and represents abnormal situations with exceptions handled at the appropriate level.

## ADDED Requirements

### Requirement: Layered packages
The code SHALL be organized in non-default Java packages with separate packages for presentation (user interaction and command processing), business (model, business rules and control elements) and persistence (repositories and reading/writing persistent information). Presentation SHALL NOT access persistence files directly, and domain objects SHALL NOT store or retrieve their own persistent representation.

#### Scenario: Package inspection
- **WHEN** the source tree is inspected
- **THEN** no class is in the default package, presentation classes do not use file APIs or persistence classes, and domain classes do not reference repositories or files

### Requirement: Exception-based handling of abnormal situations
Business operations SHALL signal rejected or failed operations by throwing exceptions that describe the situation; the presentation layer SHALL catch them, report a controlled error message and continue processing subsequent commands.

#### Scenario: Rejected operation
- **WHEN** an operation that cannot be completed, such as `TICKET CREATE` for an unknown user, is processed
- **THEN** an error is reported and the next command is processed normally

#### Scenario: Malformed command
- **WHEN** a malformed command is processed
- **THEN** a controlled error is reported and command processing continues
