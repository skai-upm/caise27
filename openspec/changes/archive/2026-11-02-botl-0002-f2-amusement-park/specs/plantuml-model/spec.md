# Spec Delta

## Purpose

Documents the Delivery 2 design as a PlantUML class diagram that corresponds to the code actually implemented.

## ADDED Requirements

### Requirement: PlantUML class diagram
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram showing the model classes, interfaces and relevant enumerations with relevant attributes, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities; methods and constructors MAY be omitted. (F2-C09)

#### Scenario: Diagram parses
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it parses without syntax errors

### Requirement: Diagram consistent with code
The diagram SHALL represent correctly the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions, with directions and multiplicities matching the implemented references, and SHALL NOT include elements absent from the code. (F2-C09)

#### Scenario: Code-to-diagram review
- **WHEN** each model class and reference in the code is compared with the diagram
- **THEN** every class, attribute, direction and multiplicity shown exists in the code and every model relationship in the code is shown
