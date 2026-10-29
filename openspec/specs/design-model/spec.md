# design-model Specification

## Purpose

Defines the PlantUML class model that documents the application design and must stay consistent with the submitted code.

## Requirements

### Requirement: PlantUML class diagram
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the Delivery 3 model showing classes, interfaces, relevant enumerations, inheritance, interface implementations, associations, compositions, relationship directions, multiplicities and relevant attributes (no methods or constructors needed), including the ticket access interfaces, generic attraction types, passes and their cardinalities, invoice builders, dependency-injection relationships and singleton controllers.

#### Scenario: File present and parseable
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists and is enclosed in `@startuml` / `@enduml`

#### Scenario: Delivery 3 elements present
- **WHEN** the diagram is inspected
- **THEN** it contains the access interfaces, the four attraction classes with their generic bounds, SpeedPass and SpecialPass with their cardinality towards IndividualTicket, the invoice builder hierarchy and the singleton controllers

### Requirement: Diagram consistent with code
The diagram SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions exactly as implemented.

#### Scenario: Every model class appears
- **WHEN** the model classes in the source are compared with the diagram
- **THEN** each domain class, enumeration and abstract ticket type appears in the diagram with the implemented relationships
