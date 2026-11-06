# layered-architecture Specification

## Purpose
Defines how the Amusement Park application is organized into presentation, business and persistence layers with one-way dependencies, and how abnormal situations are represented with exceptions and handled without ending command processing.

## Requirements

### Requirement: Separate layer packages
The application code SHALL be organized in distinct Java packages for presentation (user interaction and command processing), business (model, business rules and application control) and persistence (repositories and reading/writing persistent data), all under a named root package. No class SHALL be in the default package.

#### Scenario: Layer packages present
- **WHEN** the production sources are inspected
- **THEN** classes exist under `es.upm.park.presentation`, `es.upm.park.business` and `es.upm.park.persistence`
- **AND** every source file declares a package

### Requirement: Layer dependency rules
The presentation layer SHALL NOT access the persistence layer or persistent files directly. The business layer SHALL NOT depend on the presentation or persistence layers. Domain objects SHALL NOT store or retrieve their own persistent representation. Only the application's composition root SHALL connect presentation, business and persistence.

#### Scenario: Forbidden dependency is detected
- **GIVEN** a presentation or business class that imports a persistence class
- **WHEN** the test suite runs
- **THEN** the architecture check fails, naming the offending file

### Requirement: Exceptions represent abnormal situations
Operations SHALL signal abnormal situations by throwing exceptions that classify the problem: an element that does not exist, an operation not allowed by the business rules, a malformed input value, or persistent data that could not be stored. The presentation layer SHALL catch them and show an error message.

#### Scenario: Unknown element
- **WHEN** `GROUP DELETE;G99` is processed and `G99` does not exist
- **THEN** a not-found exception is raised by the business layer and an `ERROR:` message is displayed

#### Scenario: Rule violation
- **WHEN** a ticket for today is deleted with `TICKET DELETE`
- **THEN** a business-rule exception is raised and an `ERROR:` message is displayed

### Requirement: Abnormal situations do not stop command processing
An incorrect command, an operation that cannot be completed, or a failure to store persistent data SHALL be reported with an error message, and processing SHALL continue with the next command.

#### Scenario: Storage failure during a session
- **GIVEN** persistent data cannot be written
- **WHEN** `GROUP CREATE;G01;Familia` is processed, followed by `HELP`
- **THEN** an error message states that the data could not be saved
- **AND** the help text is displayed for the next command

#### Scenario: Behavior preserved after reorganization
- **GIVEN** the Delivery 2 integration session
- **WHEN** it is run after the reorganization
- **THEN** it produces the same results as before
