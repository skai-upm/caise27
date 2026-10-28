## MODIFIED Requirements

### Requirement: Diagram content
The diagram SHALL show the model classes with their relevant attributes (without methods or constructors), the ticket inheritance hierarchy, the composition between a group ticket and its individual tickets, and the associations among users, groups, tickets and attractions with their directions and multiplicities. For Delivery 3 it SHALL also show the ticket access interfaces, the generic attraction and its types, the relationships and cardinalities between individual tickets and SpeedPasses (any number) and Special Passes (at most one), the invoice Builder abstractions used to construct user and group invoices, the singleton controllers and the relationships created by dependency injection.

#### Scenario: Ticket hierarchy and composition
- **WHEN** the diagram is inspected
- **THEN** `IndividualTicket` and `GroupTicket` extend the abstract `Ticket`, and `GroupTicket` is composed of one or more `IndividualTicket`

#### Scenario: Directed associations
- **WHEN** the diagram is inspected
- **THEN** it shows directed associations from `IndividualTicket` to exactly one `User`, from `GroupTicket` to zero or one `Group`, from `Group` to its member `User`s and from `Attraction` to its occupant `User`s, and no association from `User` to tickets or attractions

#### Scenario: Delivery 3 elements
- **WHEN** the diagram is inspected
- **THEN** it shows `Admission`, `IndividualAdmission` and `GroupAdmission` realised by the tickets, `Attraction<T extends Admission>` with its four bound subclasses, `SpeedPass "0..*"` and `SpecialPass "0..1"` per `IndividualTicket`, `InvoiceBuilder` with `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector` and `Invoice`, `ParkController` and `InvoiceController` as singletons, and the injected repository and password-hasher dependencies

### Requirement: Consistency with the implementation
Every class, interface, record and enumeration of the business packages (model, controller, invoice, repository, security) and every persistence implementation class SHALL appear in the diagram, every attribute of the model classes SHALL appear, and the diagram SHALL NOT declare types that do not exist in the code.

#### Scenario: Automated consistency check
- **WHEN** the test suite runs
- **THEN** a check confirms that each type of those packages is declared in `uml/model.puml`, each model field is present, and every type declared in the diagram exists in the main sources
