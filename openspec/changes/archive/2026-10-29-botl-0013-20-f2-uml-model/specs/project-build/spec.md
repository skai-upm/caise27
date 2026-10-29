# Spec Delta

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain the PlantUML source file `uml/model.puml` with a class diagram of the implemented design showing its classes, interfaces and relevant enumerations, relevant attributes, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities, without methods or constructors. The diagram SHALL correspond to the code: the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions SHALL be represented as implemented.

#### Scenario: Model file present and complete
- **WHEN** the build runs its tests
- **THEN** `uml/model.puml` exists, starts with `@startuml`, ends with `@enduml`, and declares every production class of the application

#### Scenario: Key relationships represented
- **WHEN** `uml/model.puml` is inspected
- **THEN** it declares `IndividualTicket` and `GroupTicket` extending `Ticket`, a composition from `GroupTicket` to `IndividualTicket` with multiplicity `1..*`, and directed associations from `IndividualTicket` to `User`, from `GroupTicket` to `Group` (`0..1`), from `Group` to `User` and from `Attraction` to `User`
