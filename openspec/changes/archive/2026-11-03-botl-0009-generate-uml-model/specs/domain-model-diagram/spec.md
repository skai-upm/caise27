# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram derived from the current implementation that represents the implemented classes, abstract classes and enums, their inheritance, associations, aggregations and compositions with their actual navigability and multiplicities (including group membership of users, the user owning each ticket, the ticket inheritance hierarchy, the aggregation and composition of group tickets, and the users present in each attraction), and SHALL NOT contain classes, attributes or relationships that do not exist in the code nor inverse relationships absent from it.

#### Scenario: Diagram is valid
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** it is accepted without syntax errors

#### Scenario: Group relationships represented
- **WHEN** the diagram is reviewed
- **THEN** it shows `Group` associated with `User` with multiplicity `*` members, alongside the existing user, storage, service and command classes

#### Scenario: Ticket relationships represented
- **WHEN** the diagram is reviewed
- **THEN** it shows `Ticket` referencing its owning `User` with multiplicity `0..1` (absent only for historical tickets of deleted users), and the ticket service and storage classes

#### Scenario: Group ticket relationships represented
- **WHEN** the diagram is reviewed
- **THEN** it shows `Ticket` and `GroupTicket` specialising `ParkTicket`, `GroupTicket` aggregating its `Group` and composed of `1..*` `Ticket`

#### Scenario: Attraction relationships represented
- **WHEN** the diagram is reviewed
- **THEN** it shows `Attraction` with `*` present `User`s and the attraction service and storage classes

#### Scenario: No invented or inverse elements
- **GIVEN** a reference that exists only in one direction in the code (e.g. `Group` holds its members, `User` does not know its groups)
- **WHEN** the diagram is reviewed
- **THEN** only that direction is drawn and no unimplemented class appears

## ADDED Requirements

### Requirement: Diagram notation
The class diagram SHALL follow these conventions: classes show no methods; attributes have only primitive, `String`, value (`LocalDate`, `BigDecimal`, `Path`) or enumeration types and carry no technical labels (no `{readOnly}`, stereotypes or constants); a reference to another model class is drawn only as a relationship line whose name equals the holder's attribute name, with a cross at the holder end and an arrow (or diamond at the holder for aggregation/composition) marking the direction; usage (dependency) arrows appear only for classes that have no incoming relationship.

#### Scenario: Relationship labelled with the attribute name
- **GIVEN** `Ticket` holds a field `user` of type `User`
- **WHEN** the diagram is reviewed
- **THEN** there is a line from `Ticket` to `User` labelled `user`, and `Ticket` lists no `user` attribute

#### Scenario: Usage only where needed
- **GIVEN** `UserValidator` is not referenced by any attribute
- **WHEN** the diagram is reviewed
- **THEN** its usage by the services is shown, while classes already reached by a relationship have no usage arrows

#### Scenario: No methods
- **WHEN** any class body is reviewed
- **THEN** it contains attributes only
