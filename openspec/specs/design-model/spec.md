# design-model Specification

## Purpose

Documents the application's domain design as a PlantUML class diagram kept in the repository and consistent with the submitted code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the model showing classes, interfaces, relevant enumerations, relevant attributes, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities. Methods and constructors are not required.

#### Scenario: File present and well-formed
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists and starts with `@startuml` and ends with `@enduml`

### Requirement: Model consistent with code
The diagram SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the directed relationships among users, groups, tickets and attractions exactly as implemented, and SHALL also show the ticket access interfaces, the generic attraction classes and their types, the passes with their ticket relationships and cardinalities, the invoice builder abstractions, the singleton controllers and the relationships created by dependency injection.

#### Scenario: Every model class appears
- **WHEN** the classes in the model packages are compared with the diagram
- **THEN** each model class and enumeration appears in the diagram, and the diagram contains no class absent from the code

#### Scenario: Delivery 3 elements present
- **WHEN** the diagram is inspected
- **THEN** it declares the access interfaces, `Attraction<T>` and its four subclasses, `SpeedPass` and `SpecialPass` linked to `IndividualTicket` with multiplicities `*` and `0..1`, the invoice builder interface with its implementations and director, and the singleton controllers with their injected repositories
