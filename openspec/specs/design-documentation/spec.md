# design-documentation Specification

## Purpose

Keeps a PlantUML class model of the application's design in the repository, consistent with the submitted code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the Delivery 3 model consistent with the code. Beyond the Delivery 2 content (ticket hierarchy, group-ticket composition, users/groups/tickets/attractions relationships, attributes, inheritance, associations, directions, multiplicities), it SHALL show the ticket access interfaces, the generic attraction types, ticket–pass relationships and cardinalities, the invoice Builder abstractions, dependency-injection relationships, and the singleton controllers.

#### Scenario: Model present and consistent
- **WHEN** `uml/model.puml` is inspected
- **THEN** it is a `@startuml`/`@enduml` PlantUML class diagram whose classes and relationships match the domain classes in the source code
