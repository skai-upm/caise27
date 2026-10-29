# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class diagram
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the Delivery 3 model showing classes, interfaces, relevant enumerations, inheritance, interface implementations, associations, compositions, relationship directions, multiplicities and relevant attributes (no methods or constructors needed), including the ticket access interfaces, generic attraction types, passes and their cardinalities, invoice builders, dependency-injection relationships and singleton controllers.

#### Scenario: File present and parseable
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists and is enclosed in `@startuml` / `@enduml`

#### Scenario: Delivery 3 elements present
- **WHEN** the diagram is inspected
- **THEN** it contains the access interfaces, the four attraction classes with their generic bounds, SpeedPass and SpecialPass with their cardinality towards IndividualTicket, the invoice builder hierarchy and the singleton controllers
