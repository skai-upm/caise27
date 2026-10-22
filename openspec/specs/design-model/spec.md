# design-model Specification

## Purpose

Documents the implemented domain model as a PlantUML class diagram that is kept consistent with the source code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the model. It SHALL show the relevant classes, interfaces, and enumerations with their relevant attributes, inheritance and interface implementation, associations and compositions with directions and multiplicities. It SHALL include the ticket hierarchy, the composition of a group ticket from individual tickets, and the relationships among users, groups, tickets, attractions, and the central park controller.

#### Scenario: Diagram present and parseable
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists and is a single `@startuml` … `@enduml` PlantUML document

#### Scenario: Diagram matches the code
- **WHEN** the classes and fields named in the diagram are compared with the model source code
- **THEN** every diagram class exists in the code, and every listed relationship corresponds to a field of the stated direction
