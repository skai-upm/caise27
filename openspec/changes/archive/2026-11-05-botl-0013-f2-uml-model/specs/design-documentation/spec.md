# Spec Delta

## Purpose

Defines the PlantUML class model that documents the implemented design and the rules that keep it consistent with the code.

## ADDED Requirements

### Requirement: PlantUML class model in the repository
The repository SHALL contain the PlantUML source `uml/model.puml` (delimited by `@startuml` and `@enduml`) with a class diagram of the model structure. It SHALL show classes, interfaces, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities, with relevant attributes only and without methods or constructors.

#### Scenario: Model file present
- **GIVEN** a clean checkout
- **WHEN** `uml/model.puml` is read
- **THEN** it starts with `@startuml` and ends with `@enduml`, and it declares classes and interfaces

### Requirement: Model consistent with the implementation
The class model SHALL correspond to the code: every production class and interface SHALL be declared with its kind, and no element absent from the code SHALL be drawn as a class. In particular, it SHALL show the ticket hierarchy (`IndividualTicket` and `GroupTicket` extending abstract `Ticket`), the composition of a group ticket with its individual tickets (multiplicity 1..*), the one-way, optional reference from a group ticket to its group, and how users, groups, tickets and attractions relate.

#### Scenario: Every production type is drawn
- **GIVEN** the production sources under `src/main/java`
- **WHEN** they are compared with `uml/model.puml`
- **THEN** each class, abstract class and interface appears in the diagram

#### Scenario: Key relationships drawn
- **WHEN** `uml/model.puml` is inspected
- **THEN** it contains `Ticket <|-- IndividualTicket`, `Ticket <|-- GroupTicket`, a composition from `GroupTicket` to `IndividualTicket` with multiplicity `1..*`, and a directed association from `GroupTicket` to `Group` with multiplicity `0..1`

#### Scenario: Model drift detected
- **GIVEN** a new production class that is not in the diagram
- **WHEN** the test suite runs
- **THEN** the consistency check fails and names the missing class
