# application-architecture Specification

## Purpose
Defines the layered organisation of the application code and how abnormal situations are represented and handled with exceptions.

## Requirements

### Requirement: Layered packages
The code SHALL be organised in distinct Java packages for presentation (user interaction and command processing), business (model, business rules and application control) and persistence (repositories and reading/writing persistent data), under a named root package; the default package SHALL NOT be used.

#### Scenario: Packages present
- **WHEN** the main source tree is inspected
- **THEN** it contains `presentation`, `business` and `persistence` packages under the root package and no class in the default package

### Requirement: Layer dependencies
The presentation layer SHALL NOT access persistence files or repositories directly, and domain model classes SHALL NOT store or retrieve their own persistent representation.

#### Scenario: Presentation independent of persistence
- **WHEN** the imports of presentation classes are inspected
- **THEN** none refers to the persistence package or to file I/O APIs used for data storage

#### Scenario: Model independent of persistence
- **WHEN** the imports of business model classes are inspected
- **THEN** none refers to the persistence package

### Requirement: Exceptions for abnormal situations
Business and persistence operations SHALL signal abnormal situations by throwing application exceptions from a dedicated hierarchy, and the presentation layer SHALL handle them per command, reporting an error message and continuing with the next command.

#### Scenario: Rejected operation reported and processing continues
- **WHEN** a command file contains an operation rejected by a business rule followed by `HELP`
- **THEN** an error message is displayed for the rejected operation and the help listing is still displayed

#### Scenario: Typed exception for a missing element
- **WHEN** the business layer is asked to delete a group that does not exist
- **THEN** it throws a not-found application exception

### Requirement: Behaviour preserved
The reorganisation SHALL preserve the externally visible behaviour reached at the end of Delivery 2.

#### Scenario: Delivery 2 regression
- **WHEN** the Delivery 2 integration command files are executed after the reorganisation
- **THEN** they produce the same results as before
