# Spec Delta

## Purpose

Documents the application's domain design as a PlantUML class diagram kept in the repository and consistent with the submitted code.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the model showing classes, interfaces, relevant enumerations, relevant attributes, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities. Methods and constructors are not required.

#### Scenario: File present and well-formed
- **WHEN** the repository is inspected
- **THEN** `uml/model.puml` exists and starts with `@startuml` and ends with `@enduml`

### Requirement: Model consistent with code
The diagram SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the directed relationships among users, groups, tickets and attractions exactly as implemented.

#### Scenario: Every model class appears
- **WHEN** the classes in the model packages are compared with the diagram
- **THEN** each model class and enumeration appears in the diagram, and the diagram contains no class absent from the code
