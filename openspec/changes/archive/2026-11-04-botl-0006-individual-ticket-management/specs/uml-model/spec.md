# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `docs/uml/model.puml` that represents the application's classes and enums with their associations and multiplicities, including the association between groups and their member users and the association between tickets and their owning user.

#### Scenario: Group relationship present
- **WHEN** `docs/uml/model.puml` is reviewed
- **THEN** it is a valid `@startuml`/`@enduml` document declaring `Group` and `User` with a `Group` to `User` association of multiplicity `0..*` members, and each class it declares exists in the code

#### Scenario: Ticket relationship present
- **WHEN** `docs/uml/model.puml` is reviewed
- **THEN** it declares `Ticket`, `TicketManager` and `TicketFileStorage`, with each `Ticket` associated to `0..1` owning `User` and a user to `0..*` tickets
