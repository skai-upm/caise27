# design-model Specification

## Purpose

Documents the application's class design in a PlantUML diagram stored with the code and kept consistent with the implementation.

## Requirements

### Requirement: PlantUML model file
The repository SHALL contain the PlantUML source `uml/model.puml` with a class diagram of the final Delivery 2 design, showing classes, interfaces, relevant enumerations, relevant attributes, inheritance, implementations, associations, compositions, directions and multiplicities, without methods or constructors.

#### Scenario: File present and well formed
- **WHEN** `uml/model.puml` is opened
- **THEN** it starts with `@startuml`, ends with `@enduml` and declares the model classes

### Requirement: Required relationships represented
The diagram SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the directed relationships among users, groups, tickets and attractions with multiplicities.

#### Scenario: Ticket hierarchy and composition
- **WHEN** the diagram is inspected
- **THEN** `IndividualTicket` and `GroupTicket` extend abstract `Ticket`, and `GroupTicket` is composed of `1..*` `IndividualTicket`

#### Scenario: Directed relationships
- **WHEN** the diagram is inspected
- **THEN** it shows `IndividualTicket` → `User` (1), `GroupTicket` → `Group` (0..1), `Group` → `User` (0..*), `Attraction` → `User` (0..*), with no arrows from `User` or `Group` to tickets or attractions

### Requirement: Diagram consistent with code
Every class and enumeration of the model package SHALL appear in the diagram, and every attribute shown for a model class SHALL exist in that class.

#### Scenario: Automated consistency check
- **WHEN** the test suite runs
- **THEN** a test compares the diagram with the model classes and fails on any missing class or non-existent attribute
