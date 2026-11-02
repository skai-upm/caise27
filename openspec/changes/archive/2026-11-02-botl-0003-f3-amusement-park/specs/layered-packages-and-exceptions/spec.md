# Spec Delta

## Purpose

Defines the layered package organisation (presentation, business, persistence) and the use of exceptions to propagate and handle abnormal situations (F3-C01).

## ADDED Requirements

### Requirement: Layered packages
The code SHALL be organised in non-default Java packages that separate presentation (user interaction and command processing), business (model, business rules and control) and persistence (repositories and reading/writing persistent data). Presentation SHALL NOT access persistence files directly, and domain objects SHALL NOT store or retrieve their own persistent representation.

#### Scenario: Package inspection
- **WHEN** the source tree is inspected
- **THEN** every class is in a named package under a presentation, business or persistence package (or the application root that starts and wires the application), no presentation class imports the persistence package, and no business class performs file I/O

### Requirement: Exceptions for abnormal situations
Business operations SHALL signal abnormal situations by throwing exceptions from a business exception hierarchy; the presentation layer SHALL catch them and report a controlled error, after which command processing SHALL continue.

#### Scenario: Malformed or impossible operation
- **WHEN** a malformed command or an operation that cannot be completed is processed
- **THEN** an error message is reported and the next command is still processed
