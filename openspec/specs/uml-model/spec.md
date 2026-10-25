# uml-model Specification

## Purpose

Documents the implemented object-oriented design as a PlantUML class diagram kept in the repository, derived from the actual code without changing it.

## Requirements

### Requirement: Diagram file
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram that renders without errors.

#### Scenario: Diagram renders
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** a class diagram is produced without syntax errors

### Requirement: Faithful to the implementation
The diagram SHALL show the relevant classes, interfaces and enumerations that exist in the code, with existing inheritance/implementation relationships, associations, aggregations and compositions, their direction and multiplicities as navigable in the code. It SHALL NOT include elements or inverse relationships that do not exist in the code.

#### Scenario: Ticket hierarchy
- **WHEN** the diagram is compared with the code
- **THEN** the ticket abstraction and its individual and group subclasses, the group ticket's aggregation of its group and composition of its individual tickets, and the references between users, groups, tickets and attractions match the fields in the classes

### Requirement: Diagram conventions
Classes SHALL show only attributes of primitive, string, date, numeric or enumeration types, without visibility or other technical labels, and no methods. Attributes that are relationships SHALL be drawn only as relationships, labelled with the containing class's attribute name and marked with a cross at the non-navigable end. Usage dependencies SHALL be drawn only for classes that would otherwise have no incoming relationship.

#### Scenario: Relationship labels
- **WHEN** a class holds a field referencing another class
- **THEN** the diagram draws a navigable relationship from the holder, labelled with the field name, with a cross at the holder's end, and no corresponding attribute in the holder's box

### Requirement: No behaviour change
Producing the diagram SHALL NOT change the application's behaviour.

#### Scenario: Tests unchanged
- **WHEN** the diagram is added
- **THEN** `mvn test` passes with the same production code
