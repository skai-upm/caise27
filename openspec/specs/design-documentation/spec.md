# design-documentation Specification

## Purpose

Defines the design documentation that must live in the repository alongside the code — the PlantUML class model of the application — and how it must stay consistent with the implementation.

## Requirements

### Requirement: PlantUML class model file
The repository SHALL contain the PlantUML source file `uml/model.puml` with a class diagram of the current design.

#### Scenario: Model file present and valid
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists, starts with `@startuml`, ends with `@enduml` and is accepted by PlantUML without syntax errors

### Requirement: Model content
The diagram SHALL show the model's classes, abstract classes and relevant enumerations with relevant attributes (methods and constructors are not required), inheritance, associations, compositions, relationship directions and multiplicities. It SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions.

#### Scenario: Ticket hierarchy and composition
- **WHEN** the diagram is inspected
- **THEN** `IndividualTicket` and `GroupTicket` extend the abstract `Ticket`, and `GroupTicket` is linked to `IndividualTicket` by a composition with multiplicity `1..*`

#### Scenario: Directed relationships
- **WHEN** the diagram is inspected
- **THEN** it shows directed associations Group → User (members), IndividualTicket → User, GroupTicket → Group (`0..1`) and Attraction → User (occupants), and no association from User to tickets, groups or attractions

### Requirement: Model consistent with code
The diagram SHALL correspond to the code: every production class, abstract class and enumeration SHALL appear in the diagram, and the diagram SHALL NOT declare model elements that do not exist in the code.

#### Scenario: Every class appears
- **WHEN** the automated consistency check compares the production source files with the diagram
- **THEN** every production type name is declared in `uml/model.puml` and every declared type has a source file
