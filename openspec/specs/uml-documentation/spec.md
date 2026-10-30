# uml-documentation Specification

## Purpose

Documents the implemented object-oriented design of the park application as a PlantUML class diagram derived from the actual code.

## Requirements

### Requirement: PlantUML class diagram of the implementation
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram derived from the implemented code, showing the relevant classes, interfaces/abstract classes and enums, inheritance, associations and compositions with their navigability and inferable multiplicities. It SHALL NOT contain elements absent from the code nor reverse navigation that the code lacks.

#### Scenario: Diagram renders
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it renders without syntax errors

#### Scenario: Diagram matches the code
- **WHEN** the diagram is compared with the source
- **THEN** every class, relationship, direction and multiplicity shown exists in the code, including the ticket hierarchy and the references among users, groups, tickets and attractions

### Requirement: Diagram presentation rules
The diagram SHALL NOT show methods or usage relationships, except that a class with no incoming relationship SHALL show its usage. Attributes SHALL be shown without technical labels and only for primitive/enumeration types; references to other objects SHALL be drawn as relationships named after the containing attribute, with the navigability marked at the owning side.

#### Scenario: Relationship named after field
- **GIVEN** the group class holds its members in a field named `members`
- **WHEN** the diagram is inspected
- **THEN** a navigable relationship from the group to the user labelled `members` is shown and no `members` attribute appears in the group box
