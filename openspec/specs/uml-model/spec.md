# uml-model Specification

## Purpose
Keeps a PlantUML class model in the repository that documents the application's classes, their relationships and multiplicities, consistent with the implemented code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `docs/uml/model.puml` that represents the application's classes and enums with their inheritance, associations, aggregations, compositions and multiplicities, including groups and their member users, tickets and their owning user, group tickets with their group and constituent tickets, and attractions with their present users.

#### Scenario: Group relationship present
- **WHEN** `docs/uml/model.puml` is reviewed
- **THEN** it is a valid `@startuml`/`@enduml` document declaring `Group` and `User` with a `Group` to `User` association of multiplicity `0..*` members, and each class it declares exists in the code

#### Scenario: Ticket relationship present
- **WHEN** `docs/uml/model.puml` is reviewed
- **THEN** it declares `Ticket`, `TicketManager` and `TicketFileStorage`, with each individual ticket associated to `0..1` owning `User` and a user to `0..*` tickets

#### Scenario: Ticket hierarchy present
- **WHEN** `docs/uml/model.puml` is reviewed
- **THEN** abstract `Ticket` is specialised by `IndividualTicket` and `GroupTicket`, `GroupTicket` aggregates `0..1` `Group`, and `GroupTicket` is composed of `1..*` `IndividualTicket`

#### Scenario: Attractions present
- **WHEN** `docs/uml/model.puml` is reviewed
- **THEN** it declares `Attraction`, `AttractionManager` and `AttractionFileStorage`, with each attraction associated to `0..*` present `User` objects, and shows the polymorphic `getHolders()` operation on the abstract `Ticket`
