# Spec Delta

## Purpose

Documents the implemented object-oriented design as a PlantUML class diagram kept in the repository and consistent with the actual code.

## ADDED Requirements

### Requirement: PlantUML model file
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram that PlantUML can process without errors.

#### Scenario: Diagram renders
- **WHEN** PlantUML checks `uml/model.puml`
- **THEN** it reports no syntax errors

### Requirement: Diagram reflects the current code
The diagram SHALL show the relevant classes, interfaces and enumerations that exist in the code, including the implemented ticket hierarchy, and SHALL NOT invent elements that are not implemented. Producing it SHALL NOT change application behavior.

#### Scenario: Ticket hierarchy
- **WHEN** the diagram is compared with the code
- **THEN** the ticket abstraction and its concrete ticket classes appear with the same inheritance as in the code

### Requirement: Relationship notation
Relationships SHALL reflect actual navigability in the code: an association, aggregation or composition is drawn from the class holding the attribute, named exactly like that attribute, with multiplicities where they can be inferred and a cross on the non-navigable end. Inverse relationships that are not in the code SHALL NOT be drawn.

#### Scenario: Group members
- **WHEN** the group class holds a collection attribute `members` of users
- **THEN** the diagram has a relationship from the group to the user named `members` with a cross on the group end and multiplicity `0..*` on the user end

### Requirement: Attribute and method rules
Classes SHALL show only attributes of primitive-like types (numbers, booleans, strings, dates, decimals) and enumerations, without technical labels; attributes that are relationships SHALL be shown only as relationships. Methods and usage relationships SHALL NOT be shown, except that a class with no incoming relationships SHALL show its usage dependencies.

#### Scenario: No methods
- **WHEN** the diagram is inspected
- **THEN** no class lists methods

#### Scenario: Entry point usage
- **WHEN** a class such as the application entry point has no incoming relationships
- **THEN** its usage dependencies are drawn
