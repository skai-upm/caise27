# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a single PlantUML class diagram at `uml/model.puml` that is valid PlantUML and faithfully represents the current implementation: its classes, abstract classes, interfaces and enums, inheritance and implementation, associations, aggregations and compositions with their navigability and multiplicities. It SHALL NOT contain elements that do not exist in the code nor inverse relationships absent from the code.

#### Scenario: Group relationship present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it is a valid `@startuml`/`@enduml` document declaring `Group` and `User` with a navigable `Group` to `User` association named `members` of multiplicity `0..*`, and each class it declares exists in the code

#### Scenario: Ticket relationship present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it declares `Ticket`, `TicketManager` and `TicketFileStorage`, with `IndividualTicket` navigable to `0..1` `User` through `owner`, and no inverse association from `User` to tickets

#### Scenario: Ticket hierarchy present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** abstract `Ticket` is specialised by `IndividualTicket` and `GroupTicket`, `GroupTicket` aggregates `0..1` `Group` through `group`, and `GroupTicket` is composed of `1..*` `IndividualTicket` through `constituents`

#### Scenario: Attractions present
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it declares `Attraction`, `AttractionManager` and `AttractionFileStorage`, with `Attraction` navigable to `0..*` `User` through `present`

#### Scenario: Valid PlantUML
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is recognised as a class diagram without syntax errors

## ADDED Requirements

### Requirement: Diagram notation rules
The class diagram SHALL show no methods and no technical labels (such as stereotypes, `{readOnly}` or `{static}` markers). Class bodies SHALL show only attributes of primitive, value (`String`, `LocalDate`, `BigDecimal`, `Path`) or enumeration types. Every reference to another modelled class SHALL be drawn only as a relationship, named exactly as the holding attribute and drawn with a navigability arrow toward the referenced class and a cross on the holder's end, except where the holder's end carries an aggregation or composition diamond. Usage (dependency) relationships SHALL be drawn only for classes that have no incoming relationship, to show where they are used.

#### Scenario: No methods or labels
- **WHEN** the class bodies in `uml/model.puml` are reviewed
- **THEN** no operation signatures, stereotypes or `{...}` property labels appear

#### Scenario: References are relationships, not attributes
- **GIVEN** `CommandProcessor` holds a `UserManager` in attribute `users`
- **WHEN** the diagram is reviewed
- **THEN** `CommandProcessor` has no `users` attribute in its body and there is a `CommandProcessor x--> "1" UserManager : users` relationship

#### Scenario: Usage only for classes without incoming relationships
- **GIVEN** `UserValidator`, `UserField` and `CommandRunner` are not held in any attribute
- **WHEN** the diagram is reviewed
- **THEN** dashed usage relationships show which classes use them, and no usage relationship targets a class that already has an incoming relationship

### Requirement: Diagram kept in sync with code
An automated test SHALL fail when a top-level class or enum in `src/main/java` is missing from `uml/model.puml`, or the diagram declares a class that does not exist in the code.

#### Scenario: Missing class detected
- **WHEN** a new production class is added without updating `uml/model.puml`
- **THEN** `mvn test` fails, naming the missing class
