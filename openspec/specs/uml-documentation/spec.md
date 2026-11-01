# uml-documentation Specification

## Purpose

Documents the object-oriented design actually implemented in the project as a PlantUML class diagram kept in the repository.

## Requirements

### Requirement: PlantUML model file
The project SHALL contain `uml/model.puml`, a syntactically valid PlantUML class diagram derived from the implemented code after the F2 behaviours are implemented.

#### Scenario: Diagram renders
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it renders without syntax errors

### Requirement: Diagram reflects the implementation
The diagram SHALL show the relevant classes, interfaces, abstract classes, generic type parameters and their bounds, and enumerations, grouped by their layer packages, the actual inheritance and implementation relationships, and the associations, aggregations, and compositions that exist as fields, with navigability direction and multiplicities inferable from the code. It SHALL NOT contain elements or reverse navigations that do not exist in the code, and producing it SHALL NOT change functional code.

#### Scenario: Diagram matches implementation
- **GIVEN** the final F3 implementation
- **WHEN** `uml/model.puml` is compared with the source code
- **THEN** every class, interface, generic bound, relationship, and direction shown exists in the code, and the ticket and pass hierarchies and the relationships among users, groups, tickets, passes, and attractions match the fields actually present

### Requirement: Diagram presentation rules
The diagram SHALL omit methods and technical attribute labels; SHALL show as attributes only primitive, enumeration, and value types, representing references to modelled classes as relationships named after the containing field; SHALL mark each association line with a cross at the end of the class that holds the field and an arrow towards the referenced class; and SHALL show usage relationships only for classes that have no incoming relationship.

#### Scenario: Relationship naming and direction
- **GIVEN** the class holding a field `members` referencing users
- **WHEN** the diagram is inspected
- **THEN** a relationship labelled `members` goes from that class, marked with a cross on its side, to `User`, and no `members` attribute is listed inside the class
