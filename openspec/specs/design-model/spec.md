# design-model Specification

## Purpose

Documents the implemented domain model as a PlantUML class diagram that is kept consistent with the source code.

## Requirements

### Requirement: PlantUML class model
`uml/model.puml` SHALL be a PlantUML class diagram consistent with the code. It SHALL show classes, interfaces, and enumerations with their relevant attributes, inheritance, interface implementation, and associations with directions and multiplicities. It SHALL include the ticket hierarchy and group-ticket composition; users, groups, tickets, and attractions; ticket access interfaces and generic attraction types; passes with their multiplicities; the invoice builders; Singleton controllers; and injected dependencies.

#### Scenario: Diagram present and parseable
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists and is a single `@startuml` … `@enduml` PlantUML document

#### Scenario: Diagram matches the code
- **WHEN** the classes and fields named in the diagram are compared with the model source code
- **THEN** every diagram class exists in the code, and every listed relationship corresponds to a field of the stated direction

#### Scenario: Delivery 3 elements shown
- **WHEN** the diagram is inspected
- **THEN** it contains the access interfaces, the generic attraction classes, `SpeedPass` and `SpecialPass` linked to `IndividualTicket`, the invoice builder abstraction with user and group builders, and the Singleton controllers
