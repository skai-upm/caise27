# Spec Delta

## Purpose

Documents the implemented Delivery-2 design as a PlantUML class diagram kept consistent with the code.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram showing the classes, interfaces, relevant enumerations and attributes, inheritance, associations, compositions, relationship directions and multiplicities of the implemented model, without methods or constructors.

#### Scenario: Diagram parses
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it parses without syntax errors

### Requirement: Diagram matches the code
The diagram SHALL represent the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions exactly as implemented, inventing no elements.

#### Scenario: Code-to-diagram review
- **WHEN** the diagram's relationships are compared with the domain classes' fields
- **THEN** every association direction and multiplicity matches an implemented reference
