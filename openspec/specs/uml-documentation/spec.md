# uml-documentation Specification

## Purpose

Documents the object-oriented design actually implemented in the project as a PlantUML class diagram kept in the repository.

## Requirements

### Requirement: PlantUML model file
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram derived from the implemented code.

#### Scenario: Diagram renders
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it renders without syntax errors

### Requirement: Diagram reflects implementation
The diagram SHALL show the relevant classes, interfaces and enumerations, their actual inheritance/implementation, and navigable associations and compositions with direction and inferable multiplicities. It SHALL contain no element or relationship absent from the code and SHALL NOT add reverse navigation.

#### Scenario: Ticket hierarchy and relationships match code
- **WHEN** the diagram is compared with the source code
- **THEN** the ticket hierarchy and the relationships among users, groups, tickets and attractions match the fields actually present

### Requirement: Diagram presentation rules
Classes SHALL show only primitive and enumeration attributes without technical labels and no methods; relationship attributes SHALL appear only as relationships named after the holding field, with a cross marking the non-navigable end; usage relationships SHALL be shown only for classes that otherwise have no incoming relationship.

#### Scenario: Presentation conventions
- **WHEN** the diagram is inspected
- **THEN** no methods or object-typed attributes appear in classes and each association label equals the field name in the owning class

### Requirement: Documentation does not change behavior
Producing the diagram SHALL NOT modify functional code.

#### Scenario: No functional change
- **WHEN** the UML task is completed
- **THEN** only the diagram file is added or changed by that task
