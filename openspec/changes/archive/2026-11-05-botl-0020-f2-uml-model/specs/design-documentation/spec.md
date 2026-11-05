# Spec Delta

## Purpose

Documents the application's object model as a PlantUML class diagram kept consistent with the implementation, covering classes, enumerations, inheritance, compositions, associations, directions and multiplicities.

## ADDED Requirements

### Requirement: PlantUML class model in the repository
The repository SHALL contain the PlantUML source `uml/model.puml`, a syntactically valid class diagram of the Phase 2 design that shows classes, interfaces and relevant enumerations with their relevant attributes (methods and constructors are not required).

#### Scenario: Model file present and valid
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted as a valid diagram with no syntax errors

### Requirement: Required relationships represented
The diagram SHALL represent the `Ticket` inheritance hierarchy (`IndividualTicket` and `GroupTicket` extending `Ticket`), the composition between a `GroupTicket` and its `IndividualTicket`s, and the directed associations implemented in code with multiplicities: individual ticket to its user, group ticket to its optional group, group to its member users, attraction to the users inside, and central management to its repositories. No relationship from `User` to tickets, groups or attractions SHALL be shown, because users hold no such references.

#### Scenario: Ticket hierarchy and composition shown
- **WHEN** the diagram is inspected
- **THEN** it contains inheritance from `IndividualTicket` and `GroupTicket` to `Ticket` and a composition from `GroupTicket` to `IndividualTicket` with multiplicity `1..*`

#### Scenario: Directions match the code
- **WHEN** the diagram is inspected
- **THEN** associations point from `IndividualTicket` to `User`, from `GroupTicket` to `Group` (`0..1`), from `Group` to `User` and from `Attraction` to `User`, and none start at `User`

### Requirement: Diagram consistent with the implementation
Every class and enumeration of the model, repository, park-management and persistence packages that exists in the code SHALL appear in the diagram, and the diagram SHALL NOT declare model classes that do not exist in the code; this consistency SHALL be checked automatically by the test suite.

#### Scenario: Automated consistency check
- **WHEN** `mvn test` runs
- **THEN** a test verifies that each such class in `src/main/java` is declared in `uml/model.puml` and that each class declared in the diagram exists in the code
