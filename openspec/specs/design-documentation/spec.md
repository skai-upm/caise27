# design-documentation Specification

## Purpose

Keeps a PlantUML class model of the application's design in the repository, consistent with the submitted code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the Delivery 2 model. It SHALL show the classes, interfaces and relevant enumerations with their relevant attributes, and inheritance, implementations, associations, compositions, directions and multiplicities, including the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions. Methods and constructors are not required.

#### Scenario: Model present and consistent
- **WHEN** `uml/model.puml` is inspected
- **THEN** it is a `@startuml`/`@enduml` PlantUML class diagram whose classes and relationships match the domain classes in the source code
