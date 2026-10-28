# design-documentation Specification

## Purpose
Defines the design documentation that accompanies the code: a PlantUML class model of the application that must stay consistent with the implementation.

## Requirements

### Requirement: PlantUML class model file
The repository SHALL contain the PlantUML source `uml/model.puml` with a class diagram of the Delivery 2 design, showing classes, interfaces, relevant enumerations, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities, with only relevant attributes and no methods or constructors.

#### Scenario: Diagram present
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists, starts with `@startuml` and ends with `@enduml`

### Requirement: Key relationships represented
The diagram SHALL represent the ticket hierarchy (`Ticket` specialized by `IndividualTicket` and `GroupTicket`), the composition between a group ticket and its individual tickets, and the directed relationships individual ticket → user, group ticket → group (0..1), attraction → users inside, and group → member users.

#### Scenario: Ticket hierarchy and composition
- **WHEN** `uml/model.puml` is read
- **THEN** it declares `Ticket` as abstract, `IndividualTicket` and `GroupTicket` extending it, and a composition from `GroupTicket` to `IndividualTicket` with multiplicity `1..*`

### Requirement: Consistency with the code
Every model class, interface and enumeration of the domain package SHALL appear in the diagram, and every type declared in the diagram SHALL exist in the code.

#### Scenario: Automated consistency check
- **WHEN** the test suite runs
- **THEN** a test comparing the types declared in `uml/model.puml` with the classes in the code passes
