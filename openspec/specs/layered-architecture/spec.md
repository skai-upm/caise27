# layered-architecture Specification

## Purpose

Defines how the application is organised into presentation, business and persistence layers and how abnormal situations are represented with exceptions and handled without stopping command processing.

## Requirements

### Requirement: Layered packages
The code SHALL be organised in distinct Java packages for presentation (user interaction and command processing), business (model, business rules and control elements) and persistence (repositories and reading/writing persistent information), with no class in the default package.

#### Scenario: Package inspection
- **WHEN** the production sources are inspected
- **THEN** every class belongs to `es.upm.amusementpark.presentation`, `es.upm.amusementpark.business` or `es.upm.amusementpark.persistence` (or their subpackages)

### Requirement: Layer responsibilities respected
The presentation layer SHALL NOT access persistence files or repositories directly, and domain objects SHALL NOT store or retrieve their persistent representation.

#### Scenario: Dependency inspection
- **WHEN** the presentation and business-model sources are inspected
- **THEN** presentation imports no persistence class other than the persistence failure exception, and the model imports nothing from persistence

### Requirement: Exceptions for abnormal situations
Business operations SHALL signal abnormal situations (missing elements, invalid data, duplicates, operations not allowed in the current state) by throwing exceptions, which the presentation layer SHALL catch and report as error messages so that command processing continues.

#### Scenario: Business rule violation
- **WHEN** `USER DELETE` targets a user with an active ticket
- **THEN** the business layer throws an exception, the output is an `ERROR: User has active tickets` line and the next command is processed

#### Scenario: Syntax error
- **WHEN** a command line has the wrong number of fields or an unknown command name
- **THEN** an `ERROR:` line is printed and processing continues

### Requirement: Delivery 2 behaviour preserved
After the reorganisation every Delivery 1 and Delivery 2 command SHALL produce the same results and outputs as before.

#### Scenario: Regression suite
- **WHEN** the existing automated tests of Deliveries 1 and 2 run against the reorganised code
- **THEN** they all pass
