# domain-model-diagram Specification

## Purpose

Keeps a PlantUML class model in the repository that represents the application's classes, relationships and multiplicities as they currently are.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `docs/uml/model.puml` that represents every production class and enum, their relationships, and the multiplicities of their associations, consistent with the current source code.

#### Scenario: Diagram covers groups
- **WHEN** `docs/uml/model.puml` is reviewed after this change
- **THEN** it contains `User`, `Group`, `GroupManager`, `UserManager` and the other production classes, a `Group` to `User` association with multiplicities `0..*` on both ends, and a `GroupManager` to `Group` composition with `0..*` groups

#### Scenario: Diagram covers tickets
- **WHEN** `docs/uml/model.puml` is reviewed after the individual-ticket change
- **THEN** it contains `Ticket`, `TicketManager` and `TicketFileRepository`, a `Ticket` to `User` owner association with multiplicities `0..*` tickets to `0..1` user, and a `TicketManager` to `Ticket` composition with `0..*` tickets

#### Scenario: Diagram is valid PlantUML
- **WHEN** the file is read
- **THEN** it starts with `@startuml` and ends with `@enduml`
