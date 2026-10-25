# uml-model Specification

## Purpose

Documents the implemented object-oriented design as a PlantUML class diagram kept in the repository.

## Requirements

### Requirement: PlantUML model file
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram that represents the current implementation without inventing elements that do not exist in the code.

#### Scenario: Diagram is valid
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted without syntax errors

#### Scenario: Diagram matches code
- **WHEN** the classes, interfaces and enumerations of the domain and architecture are compared with the diagram
- **THEN** every relevant type, inheritance and association in the code appears and nothing absent from the code is shown

### Requirement: Diagram notation rules
The diagram SHALL show classes, interfaces, enumerations, inheritance, associations, aggregations and compositions with navigability and multiplicities as found in the code. Attributes SHALL only be primitive values or enumerations, without visibility or other technical labels; attributes that hold references to other classes SHALL be drawn only as relationships named after the attribute, with a cross on the non-navigable end. Methods and usage relationships SHALL NOT be shown, except that a class with no incoming relationship SHALL show its usage.

#### Scenario: Ticket holder relationship
- **WHEN** the individual ticket class holds a `user` attribute referencing a user
- **THEN** the diagram draws a relationship named `user` from the ticket to the user, crossed at the ticket end, and no `user` attribute inside the ticket class

#### Scenario: Entry-point class
- **WHEN** a class such as the application entry point has no incoming relationship
- **THEN** the diagram shows the usage relationship from it to the classes it uses
