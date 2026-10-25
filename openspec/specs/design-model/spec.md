# design-model Specification

## Purpose

Keeps a PlantUML class diagram in the repository that faithfully documents the implemented object-oriented design.

## Requirements

### Requirement: PlantUML model file
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram derived from the actual implementation, showing relevant classes, interfaces, enumerations, inheritance/implementation, associations, aggregations and compositions with their direction and multiplicities, without inventing unimplemented elements.

#### Scenario: Diagram is valid
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is parsed without syntax errors

#### Scenario: Ticket hierarchy matches code
- **WHEN** the diagram is compared with the source
- **THEN** the ticket classes and their inheritance match the implemented hierarchy

### Requirement: Diagram conventions
The diagram SHALL NOT show methods or usage relationships, except that a usage dependency SHALL be shown for a class that has no incoming relationship. Attributes SHALL be shown without technical labels and only for primitive values, strings, dates, numbers and enumerations; references to other classes SHALL be shown as relationships named after the holding attribute, with a cross at the holder end marking navigability.

#### Scenario: Relationship naming
- **WHEN** a class holds a collection attribute `users` of `User`
- **THEN** the diagram shows a relationship labelled `users` from that class to `User`, crossed at the holder end, and no `users` attribute

### Requirement: Documentation only
Producing or updating the diagram SHALL NOT change application behavior.

#### Scenario: Tests unaffected
- **WHEN** the diagram is added
- **THEN** `mvn test` passes with no source changes made for the diagram
