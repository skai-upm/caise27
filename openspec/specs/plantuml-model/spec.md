# plantuml-model Specification

## Purpose

Requires a PlantUML class diagram of the Delivery 2 model, kept consistent with the implemented code (source F2-C09, section 14).

## Requirements

### Requirement: PlantUML class model file
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the implemented model showing classes, interfaces, relevant enumerations, relevant attributes, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities; methods and constructors need not be included.

#### Scenario: Diagram parses
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is parsed without syntax errors

### Requirement: Diagram consistent with the code
The diagram SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions with the directions and multiplicities actually implemented, without elements absent from the code.

#### Scenario: Code-to-diagram review
- **WHEN** each class, field-based relationship and multiplicity in the diagram is compared with the source code
- **THEN** every diagram element exists in the code with the same direction and multiplicity
