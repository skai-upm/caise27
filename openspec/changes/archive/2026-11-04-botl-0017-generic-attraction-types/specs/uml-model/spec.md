# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a single PlantUML class diagram at `uml/model.puml` that is valid PlantUML and faithfully represents the current implementation: its classes, abstract classes, interfaces and enums, inheritance and implementation, associations, aggregations and compositions with their navigability and multiplicities. It SHALL NOT contain elements that do not exist in the code nor inverse relationships absent from the code.

#### Scenario: Group relationship present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it is a valid `@startuml`/`@enduml` document declaring `Group` and `User` with a navigable `Group` to `User` association named `members` of multiplicity `0..*`, and each class it declares exists in the code

#### Scenario: Ticket relationship present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it declares `Ticket`, `TicketManager` and `TicketFileStorage`, with `IndividualTicket` navigable to `0..1` `User` through `owner`, and no inverse association from `User` to tickets

#### Scenario: Ticket hierarchy present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** abstract `Ticket` is specialised by `IndividualTicket` and `GroupTicket`, `GroupTicket` aggregates `0..1` `Group` through `group`, and `GroupTicket` is composed of `1..*` `IndividualTicket` through `constituents`

#### Scenario: Attractions present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it declares `Attraction`, `AttractionManager` and `AttractionFileStorage`, with `Attraction` navigable to `0..*` `User` through `present`

#### Scenario: Valid PlantUML
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is recognised as a class diagram without syntax errors

#### Scenario: Attraction types and access interfaces present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it declares the access interfaces implemented by the ticket classes and the generic abstract attraction specialised by the four attraction type classes
