# Spec Delta

## Purpose

Documents the implemented Delivery 2 design as a PlantUML class diagram kept consistent with the code.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the implemented model showing classes, interfaces, relevant enumerations, inheritance, interface implementations, associations, compositions, relationship directions, multiplicities and relevant attributes; methods and constructors need not be shown.

#### Scenario: Diagram renders
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it parses without syntax errors

### Requirement: Diagram matches the code
The diagram SHALL correspond to the submitted code, in particular the ticket hierarchy, the composition between a group ticket and its individual tickets, and the directed relationships among users, groups, tickets and attractions, without elements absent from the implementation.

#### Scenario: Code-to-diagram review
- **WHEN** every class and relationship in the diagram is compared with the source
- **THEN** each one exists in the code with the same direction and multiplicity
