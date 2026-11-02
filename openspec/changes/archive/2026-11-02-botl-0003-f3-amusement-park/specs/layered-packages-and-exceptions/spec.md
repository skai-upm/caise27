# Spec Delta

## Purpose

Organizes the application into distinguishable presentation, business and persistence layers and uses exceptions to propagate abnormal situations that are handled without stopping command processing.

## ADDED Requirements

### Requirement: Layered packages
The code SHALL be organized in non-default Java packages with separate packages for presentation (user interaction and command processing), business (model, business rules and controllers) and persistence (repositories and file reading/writing); presentation SHALL NOT access persistence files directly and domain objects SHALL NOT store or retrieve their own persistent representation. (F3-C01)

#### Scenario: Package inspection
- **WHEN** the source tree is inspected
- **THEN** no class is in the default package, presentation and business classes perform no file reading or writing of data files, and the business layer does not depend on the persistence package

### Requirement: Controlled exception handling
Abnormal situations in business operations SHALL be represented by exceptions propagated to the command-processing level, where they are reported as an error and processing continues with the next command. (F3-C01)

#### Scenario: Malformed or rejected operation
- **WHEN** a malformed command or a rejected operation is executed
- **THEN** a controlled error message is shown and the following commands are processed
