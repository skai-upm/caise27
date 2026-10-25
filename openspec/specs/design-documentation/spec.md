# design-documentation Specification

## Purpose

Keeps a PlantUML class model in the repository that faithfully documents the object-oriented design actually implemented.

## Requirements

### Requirement: PlantUML model file
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram that PlantUML can process without errors.

#### Scenario: Diagram renders
- **WHEN** PlantUML checks `uml/model.puml`
- **THEN** it reports no syntax errors

### Requirement: Faithful to the implementation
The diagram SHALL show the relevant classes, interfaces, abstract classes and enumerations that exist in the code, with their inheritance and implementation relationships, and SHALL NOT include elements, attributes or relationships that are not implemented. Producing it SHALL NOT change application behaviour.

#### Scenario: Ticket hierarchy
- **WHEN** the diagram is compared with the code
- **THEN** it shows the abstract ticket type and its individual and group subclasses exactly as implemented

### Requirement: Relationship conventions
Relationships SHALL follow the code's actual navigability, without invented inverse relationships. Each association, aggregation or composition SHALL be named after the attribute of the holding class, show multiplicities where inferable, and mark direction with an arrowhead at the referenced class and a cross (or the aggregation/composition diamond) at the holding class.

#### Scenario: Group members
- **WHEN** the relation between groups and users is inspected
- **THEN** it is a single directed relation from the group class to the user class named after the group's member list attribute with multiplicity `*`

### Requirement: Attribute and usage conventions
Classes SHALL list only attributes of primitive, string, date, decimal or enumeration type, without visibility or other technical labels; attributes implementing a relationship SHALL appear only as relationships. Methods and usage dependencies SHALL NOT be shown, except that a class with no incoming relationship SHALL show a usage dependency from the class that uses it.

#### Scenario: No methods and no relationship attributes
- **WHEN** the user class and the ticket classes are inspected
- **THEN** they show no methods, and the ticket's user appears as an association, not as an attribute
