# domain-model Specification

## Purpose

Keeps a PlantUML class model of the application's domain in the repository so the object-oriented design can be reviewed alongside the code.

## Requirements

### Requirement: PlantUML domain model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram of the domain classes and their relationships with direction and multiplicities, updated whenever domain classes or relationships change. The diagram SHALL represent only elements present in the code.

#### Scenario: Model reflects groups
- **WHEN** `uml/model.puml` is processed by PlantUML after this change
- **THEN** it parses without errors and shows `User`, `Group` and the group-to-users association with multiplicity `0..*`

#### Scenario: Nothing invented
- **WHEN** each class, attribute and relationship in the diagram is compared with the source code
- **THEN** every one of them exists in the code with the same name

### Requirement: Diagram conventions
The model SHALL show classes, interfaces, enumerations and inheritance; SHALL NOT show methods; SHALL show as attributes only primitive or value data and enumerations, without technical labels; SHALL draw every reference held in an attribute as a relationship named after that attribute, with a cross at the holder's end and an arrow at the referenced end (composition and aggregation keep their diamond at the holder end); and SHALL show usage dependencies only towards classes that have no incoming relationship.

#### Scenario: Relationship named after attribute
- **WHEN** `Group` holds its users in attribute `members`
- **THEN** the diagram has a line from `Group` (cross end) to `User` (arrow end) labelled `members` with multiplicity `0..*`, and `Group` lists no `members` attribute

#### Scenario: Ticket hierarchy
- **WHEN** the ticket classes are drawn
- **THEN** abstract `Ticket` generalizes `IndividualTicket` and `GroupTicket`, `GroupTicket` aggregates its `group` and composes its `tickets`
