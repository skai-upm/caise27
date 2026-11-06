# design-documentation Specification

## Purpose
Keeps an up-to-date PlantUML class model of the Amusement Park design in the repository, so reviewers can check the structure and relationships against the implemented code.

## Requirements

### Requirement: PlantUML class model in the repository
The repository SHALL contain the PlantUML source file `uml/model.puml` with a class diagram of the model structure. It SHALL show classes, interfaces, records and enumerations where relevant, with their relevant attributes but without methods or constructors.

#### Scenario: Model file present and valid
- **GIVEN** a clean checkout
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is a syntactically valid class diagram enclosed in `@startuml` / `@enduml`

### Requirement: Diagram represents the ticket hierarchy and relationships
The diagram SHALL show the common ticket abstraction with individual and group tickets as its subtypes, the composition between a group ticket and its individual tickets with multiplicities, the one-way reference from a group ticket to its group (optional), and how groups, tickets and attractions refer to users, each with direction and multiplicity.

#### Scenario: Ticket hierarchy and composition
- **WHEN** the diagram is inspected
- **THEN** `IndividualTicket` and `GroupTicket` extend the abstract `Ticket`
- **AND** `GroupTicket` is composed of `1..*` `IndividualTicket` and navigates to `0..1` `Group`

#### Scenario: User references
- **WHEN** the diagram is inspected
- **THEN** it shows that groups, individual tickets and attractions reference users through their email, and that users hold no references back

### Requirement: Diagram corresponds to the code
Every class, interface and record of the business layer (model, controller and exception packages) and of the persistence layer SHALL appear in the diagram, grouped by layer package, and the diagram SHALL NOT show model elements that do not exist in the code.

#### Scenario: Missing class is detected
- **GIVEN** a class is added to the model package without updating the diagram
- **WHEN** the test suite runs
- **THEN** the UML consistency check fails, naming the missing class

#### Scenario: Diagram uses layer packages
- **WHEN** the diagram is inspected
- **THEN** its packages are `es.upm.park.business.*`, `es.upm.park.persistence` and `es.upm.park.presentation`

### Requirement: Diagram represents the Delivery 3 design
The diagram SHALL make it possible to identify the ticket access interfaces, the generic attraction class and its type-specific subclasses, the relationships between individual tickets and passes with their cardinalities (any number of SpeedPasses and at most one Special Pass per individual ticket), the invoice builder abstraction with its user and group builders, the relationships established by dependency injection, and the controllers designed as single instances.

#### Scenario: Pass cardinalities
- **WHEN** the diagram is inspected
- **THEN** an individual ticket relates to `0..*` SpeedPasses and `0..1` Special Pass

#### Scenario: Delivery 3 elements present
- **WHEN** the diagram is inspected
- **THEN** it shows `AccessTicket`, `IndividualAccess`, `GroupAccess`, `Attraction<T extends AccessTicket>` with its four subclasses, `InvoiceBuilder` with `UserInvoiceBuilder` and `GroupInvoiceBuilder`, relationships labelled as injected, and `ParkController` and `InvoiceController` marked as singletons
