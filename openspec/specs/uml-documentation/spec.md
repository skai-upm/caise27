# uml-documentation Specification

## Purpose

Documents the object-oriented design actually implemented in the project as a PlantUML class diagram kept in the repository.

## Requirements

### Requirement: PlantUML model file
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram that PlantUML can process without errors.

#### Scenario: Diagram renders
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted without syntax errors

### Requirement: Diagram reflects the implementation
The diagram SHALL show the project's relevant classes, interfaces, abstract classes, and enumerations with their real inheritance/implementation relationships and navigable associations and compositions, with direction and multiplicities inferable from the code. It SHALL NOT contain elements or reverse navigations absent from the code, and functional code SHALL NOT be changed to suit the diagram.

#### Scenario: Ticket hierarchy and relationships
- **WHEN** the diagram is compared with the source code
- **THEN** every class, generalization, association, and composition in the diagram corresponds to a type or field in the code, including the ticket hierarchy and the user, group, ticket, and attraction references

### Requirement: Diagram presentation rules
The diagram SHALL NOT show methods or usage dependencies, except usage of a class that has no incoming relationship; attributes SHALL be limited to primitive, string, date, number, and enumeration values without visibility markers; relationship names SHALL equal the holding field names; and association ends SHALL mark the holding side with a cross.

#### Scenario: Presentation conventions
- **WHEN** the diagram is inspected
- **THEN** no class shows methods, no attribute names an object relationship, each relationship label equals a field name, and each association has a cross at the holder's end
