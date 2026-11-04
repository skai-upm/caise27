# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram derived from the current implementation, representing the application's current classes, including `Group`, the ticket hierarchy, `Attraction` and their relationships with `User`, and the classes that manage and persist them, with multiplicities, and SHALL NOT contain classes, attributes or relationships that do not exist in the code.

#### Scenario: Model is valid and current
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** it parses without errors and contains `Group`, `GroupManager`, `GroupFileStore` and a `Group`–`User` association with multiplicities on both ends

#### Scenario: Ticket classes represented
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it contains `TicketManager`, `TicketFileStore` and an individual-ticket–`User` association where each individual ticket refers to exactly one user and a user has zero or more tickets

#### Scenario: Group ticket relationships represented
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it shows abstract `Ticket` specialized by `IndividualTicket` and `GroupTicket`, an aggregation from `GroupTicket` to `Group` (each group ticket belongs to exactly one group), a composition from `GroupTicket` to its one or more `IndividualTicket`s, and `GroupTicketFileStore`

#### Scenario: Attractions represented
- **WHEN** `uml/model.puml` is reviewed
- **THEN** it contains `Attraction` with its association to the `User`s present (0..* per attraction), `AttractionManager` and `AttractionFileStore`

#### Scenario: Nothing invented
- **WHEN** every class, attribute and relationship in the diagram is compared with the Java sources
- **THEN** each one exists in the code, and no class absent from the code appears

## ADDED Requirements

### Requirement: Class bodies show attributes only
Class bodies SHALL list only relevant instance attributes whose types are primitive or value types (such as `String`, `int`, `boolean`, `LocalDate`, `BigDecimal`, `Path`, collections of strings) or enumerations, without methods and without technical labels such as `{readOnly}` or `{static}`; an attribute that refers to a modelled class SHALL appear only as a relationship.

#### Scenario: No methods and no duplicated references
- **WHEN** the class bodies are reviewed
- **THEN** no method signature appears, and attributes such as `Group.members` or `IndividualTicket.user` appear only as relationship lines, not as fields

### Requirement: Relationships follow the code
Each relationship SHALL go from the class that holds the attribute to the referenced class, be named exactly as that attribute, mark its direction with a cross at the holder end (or a diamond for aggregation/composition) and an arrowhead at the referenced end, show multiplicities inferred from the field type (single reference `1`, collection `0..*` or `1..*`), and SHALL NOT include inverse relationships that the code does not hold.

#### Scenario: Navigability and names
- **WHEN** the `Group`–`User` relationship is reviewed
- **THEN** it is drawn from `Group` to `User`, named `members`, with a cross at the `Group` end, an arrowhead at `User`, and no relationship from `User` to `Group`

### Requirement: Usage only for otherwise unconnected classes
Usage (dependency) relationships SHALL be shown only for classes that have no incoming relationship, so that every class is connected to the classes that use it, and for no other class.

#### Scenario: Utility and entry classes connected
- **WHEN** the diagram is reviewed
- **THEN** `UserValidator`, `UserField`, `Result` and the file-store `Contents` records each have dependency arrows from the classes that use them, and `App` shows its usage of `CommandRunner`, while classes with an association, aggregation, composition or generalization pointing to them have no dependency arrows
