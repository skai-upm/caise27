# layered-architecture Specification

## Purpose

Defines how the Amusement Park code is organized into presentation, business and persistence layers, which dependencies between them are allowed, and where abnormal situations are handled.

## Requirements

### Requirement: Layer packages
The production code SHALL be organized in separate Java packages under `es.upm.park`: `presentation` for user interaction and command processing; `business` (and its subpackages) for the model, business rules and application control; `persistence` (and its subpackages) for repositories and reading and writing persistent information. No class MAY be in the default package or directly in `es.upm.park`.

#### Scenario: Every class belongs to a layer
- **WHEN** the production sources are inspected
- **THEN** every class is declared in `es.upm.park.presentation`, `es.upm.park.business` or `es.upm.park.persistence` (or a subpackage of them), and none is in the default package

#### Scenario: Executable JAR still starts
- **WHEN** `mvn package` is run and `java -jar target/amusement-park.jar` is started
- **THEN** the application starts from the presentation entry point exactly as before

### Requirement: Allowed dependencies between layers
The presentation layer MUST NOT read or write persistence files directly nor use file-store classes; domain model classes MUST NOT depend on persistence; business and persistence MUST NOT depend on presentation.

#### Scenario: Dependency direction checked
- **WHEN** the imports of the production sources are inspected
- **THEN** no presentation class imports a file store, no `business.model` class imports `es.upm.park.persistence`, and no business or persistence class imports `es.upm.park.presentation`

### Requirement: Exceptions handled at the presentation boundary
Abnormal situations in operations SHALL be represented and propagated as exceptions from the business and persistence layers, and handled in a controlled way by the presentation layer for each command, which reports them as messages; an incorrect command or an operation that cannot be completed MUST NOT stop command processing.

#### Scenario: Failing operations do not stop processing
- **GIVEN** a command file containing an unknown command, a rejected business operation and a valid command
- **WHEN** it is processed
- **THEN** an error message is printed for each of the first two lines and the valid command is executed

### Requirement: Behavior preserved
The reorganization MUST preserve the externally observable behavior of Delivery 2 for every command, input mode and persistence format.

#### Scenario: Existing test suite unchanged in behavior
- **WHEN** the existing behavioral tests are run after the reorganization
- **THEN** they pass with the same expected outputs
