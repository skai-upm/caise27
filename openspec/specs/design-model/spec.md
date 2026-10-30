# design-model Specification

## Purpose

Defines the PlantUML class model that documents the implemented Delivery 2 domain design and must stay consistent with the code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the model with relevant attributes, classes, interfaces, enumerations, inheritance, interface implementations, associations, compositions, directions and multiplicities, including the ticket hierarchy, group ticket composition and the user/group/ticket/attraction relationships.

#### Scenario: Diagram matches the code
- **WHEN** `uml/model.puml` is compared with the model classes in the source code
- **THEN** every class, enumeration and relationship it shows exists in the code with the same name, direction and multiplicity
