## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a valid PlantUML class diagram at `uml/model.puml` that represents exactly the current implementation: its classes, abstract classes, interfaces and enumerations, inheritance/implementation, and associations, aggregations and compositions with direction and inferable multiplicities, inventing no element and adding no inverse relationship. Classes SHALL show no methods; attributes SHALL be only primitive-like values or enumerations, without technical labels.

#### Scenario: Model contains the group relationship
- **WHEN** `uml/model.puml` is inspected
- **THEN** it declares `User`, `UserField`, `Group` and the services, repositories and controller, and shows the relationship from `Group` to `User` labelled `members` with multiplicity `0..*`

#### Scenario: Model is valid PlantUML
- **WHEN** the file is processed by PlantUML
- **THEN** it is accepted without syntax errors, and every class referenced by a relationship is declared

#### Scenario: Ticket hierarchy matches the code
- **WHEN** the diagram is inspected
- **THEN** `Ticket` is abstract and generalises `IndividualTicket` and `GroupTicket`, `GroupTicket` is composed of `IndividualTicket`s through `constituents`, and both `IndividualTicket.user` and `Attraction.present` point to `User`

#### Scenario: Relationship labels and direction follow the fields
- **WHEN** any association, aggregation or composition in the diagram is checked against the source
- **THEN** its label is the name of a field of the class at the holder end (marked with a cross or a diamond), whose type is the class at the arrow end or a collection of it

#### Scenario: Objects are not duplicated as attributes
- **WHEN** a class body is inspected
- **THEN** it contains no attribute whose type is a class of the model, and no method

#### Scenario: Usage only for classes without incoming relationships
- **WHEN** a class has no incoming relationship (for example `UserValidator` or `UserField`)
- **THEN** the diagram shows its usage with a dependency arrow, and no dependency arrows are shown for other classes

#### Scenario: Behaviour unchanged
- **WHEN** the test suite is run before and after regenerating the diagram
- **THEN** the same tests pass
