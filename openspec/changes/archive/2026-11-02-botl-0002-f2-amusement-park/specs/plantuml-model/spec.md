# Spec Delta

## Purpose

Defines the PlantUML class diagram that documents the implemented Delivery 2 model and must stay consistent with the code (F2-C09).

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram of the implemented model showing its classes, interfaces and relevant enumerations with relevant attributes, inheritance, interface implementations, associations, compositions, relationship directions and multiplicities. It SHALL show the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions. Methods and constructors MAY be omitted.

#### Scenario: Diagram parses
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is syntactically valid

#### Scenario: Diagram matches code
- **WHEN** the diagram is compared with the source code
- **THEN** every class, inheritance, association direction and multiplicity shown exists in the code, and attraction→user, individual ticket→user, group ticket→group and group ticket◆individual ticket are drawn with their implemented directions
