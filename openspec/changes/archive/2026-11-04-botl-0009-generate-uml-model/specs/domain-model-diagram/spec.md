# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `uml/model.puml` that represents the current implementation: every production class, interface, abstract class and enum, their inheritance and implementation relationships, and their associations, aggregations and compositions with direction and multiplicities as far as they can be inferred from the code. It SHALL NOT contain classes, attributes or relationships that do not exist in the code, and SHALL NOT add inverse relationships that the code does not navigate.

#### Scenario: Diagram covers groups
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it contains `User`, `UserField`, `Group`, `GroupManager`, `UserManager` and every other production class, no class absent from `src/main/java`, a `Group` to `User` aggregation labelled `members` with `0..*` users, a `GroupManager` to `Group` composition labelled `groups` with `0..*` groups, and no relationship from `User` to `Group`

#### Scenario: Diagram covers tickets
- **WHEN** the ticket classes are reviewed in `uml/model.puml`
- **THEN** it contains `Ticket`, `IndividualTicket`, `GroupTicket`, `TicketManager`, `TicketFileRepository` and `GroupTicketFileRepository`, an `IndividualTicket` to `User` association labelled `user` with `0..1` user, and `TicketManager` compositions labelled `tickets` (to `IndividualTicket`) and `groupTickets` (to `GroupTicket`) with `0..*` each

#### Scenario: Diagram covers group tickets
- **WHEN** the ticket hierarchy is reviewed in `uml/model.puml`
- **THEN** `IndividualTicket` and `GroupTicket` inherit from the abstract `Ticket`, `GroupTicket` is composed of `1..*` `IndividualTicket` through `parts`, and aggregates `0..1` `Group` through `group`

#### Scenario: Diagram covers attractions
- **WHEN** the attraction classes are reviewed in `uml/model.puml`
- **THEN** it contains `Attraction`, `AttractionManager` and `AttractionFileRepository`, an `Attraction` to `User` aggregation labelled `present` with `0..*` users, an `AttractionManager` to `Attraction` composition labelled `attractions`, and no relationship from `User` to `Attraction`

#### Scenario: Every production class is in the model
- **WHEN** the test suite runs
- **THEN** a test fails if any class in `src/main/java` is not declared in `uml/model.puml`

#### Scenario: Diagram is valid PlantUML
- **WHEN** the file is processed by PlantUML
- **THEN** it is accepted as a class diagram, starting with `@startuml` and ending with `@enduml`

## ADDED Requirements

### Requirement: Diagram notation conventions
The class model SHALL follow these conventions:
- Classes show attributes only, without methods.
- Attributes are written as `name : Type`, without visibility marks or technical labels, and only for primitive or datatype values and enums.
- A field holding a model object or a collection of them is drawn only as a relationship named exactly like the field, and is not repeated as an attribute.
- Every relationship marks its non-navigable end with a cross, or the holder end with a diamond for aggregation and composition, and points to the referenced class.
- Usage (dependency) relationships are drawn only to show how classes with no other incoming relationship are used.

#### Scenario: No methods or duplicated attributes
- **WHEN** the class bodies are reviewed
- **THEN** they contain no operations, and no attribute whose type is a model class or a collection of model classes

#### Scenario: Relationship names follow attributes
- **WHEN** the relationship from `CommandRunner` to `CommandProcessor` is reviewed
- **THEN** it is labelled `processor`, has a cross at the `CommandRunner` end and an arrow at `CommandProcessor`

#### Scenario: Usage only for classes without incoming relationships
- **WHEN** usage arrows are reviewed
- **THEN** they only point to classes that have no association, aggregation, composition or generalization pointing to them, such as `UserValidator`, `UserField`, `UserException` and `CommandRunner`
