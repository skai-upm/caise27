## MODIFIED Requirements

### Requirement: PlantUML class model in the repository
The repository SHALL contain the PlantUML source `uml/model.puml`, a syntactically valid class diagram of the final Delivery 3 design that shows classes, interfaces and relevant enumerations with their relevant attributes (methods and constructors are not required).

#### Scenario: Model file present and valid
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted as a valid diagram with no syntax errors

### Requirement: Required relationships represented
The diagram SHALL represent the `Ticket` inheritance hierarchy (`IndividualTicket` and `GroupTicket` extending `Ticket`), the composition between a `GroupTicket` and its `IndividualTicket`s, and the directed associations implemented in code with multiplicities: individual ticket to its user, group ticket to its optional group, group to its member users, attraction to the users inside, and central management to its injected repositories. For Delivery 3 it SHALL also make it possible to identify: the ticket access interfaces and which ticket classes implement them; the generic attraction type with its bounded type parameter and the attraction types binding it; the relationships between individual tickets and passes with their cardinalities (any number of SpeedPasses and at most one Special Pass per individual ticket); the Builder pattern abstractions used to construct user and group invoices; the relevant dependency-injection relationships (composition root, injected repositories and collaborators); and the controllers whose uniqueness is part of the design (singletons). No relationship from `User` to tickets, groups or attractions SHALL be shown, because users hold no such references.

#### Scenario: Ticket hierarchy and composition shown
- **WHEN** the diagram is inspected
- **THEN** it contains inheritance from `IndividualTicket` and `GroupTicket` to `Ticket` and a composition from `GroupTicket` to `IndividualTicket` with multiplicity `1..*`

#### Scenario: Directions match the code
- **WHEN** the diagram is inspected
- **THEN** associations point from `IndividualTicket` to `User`, from `GroupTicket` to `Group` (`0..1`), from `Group` to `User` and from `Attraction` to `User`, and none start at `User`

#### Scenario: Delivery 3 design elements identifiable
- **WHEN** the diagram is inspected
- **THEN** it shows `GeneralAccess`, `IndividualAccess` and `GroupAccess` realized by the ticket classes, `Attraction<T extends GeneralAccess>` specialized by `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction` and `SpecialAttraction`
- **AND** `SpeedPass "*" --> "1" IndividualTicket` and `SpecialPass "0..1" --> "1" IndividualTicket`
- **AND** `InvoiceBuilder` specialized by `UserInvoiceBuilder` and `GroupInvoiceBuilder` and directed by `InvoiceManager`
- **AND** `ParkConfiguration` injecting the repositories and controllers, and the controllers marked as singletons
