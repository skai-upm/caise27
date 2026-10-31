# uml-documentation Specification

## Purpose

Documents the object-oriented design actually implemented in the project as a PlantUML class diagram kept in the repository.

## Requirements

### Requirement: PlantUML model file
The project SHALL contain `uml/model.puml`, a PlantUML class diagram that PlantUML processes without syntax errors.

#### Scenario: Diagram is valid
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** no syntax error is reported

### Requirement: Diagram reflects the implementation
The diagram SHALL show the relevant domain and architecture classes, interfaces and enumerations with their actual inheritance and implementation relationships, and associations, aggregations and compositions with the navigability and multiplicities present in the code. It SHALL NOT contain elements or reverse navigations that are not implemented.

#### Scenario: Ticket hierarchy and relationships
- **WHEN** the diagram is compared with the code
- **THEN** the ticket hierarchy, and the references among users, groups, tickets and attractions, match the fields actually declared in the classes

### Requirement: Diagram presentation rules
The diagram SHALL NOT show methods or usage relationships, except that a class with no incoming relationship SHALL show its usage dependencies. Attributes SHALL be limited to primitives, strings, dates, decimals and enumerations, without technical labels; object references SHALL be drawn as relationships named after the holding field, with a cross on the holding end marking direction.

#### Scenario: Relationship naming and direction
- **WHEN** a class holds a field referencing another class
- **THEN** the diagram shows a relationship labelled with that field name, crossed at the holding class end and pointing to the referenced class, and the field is not repeated as an attribute
