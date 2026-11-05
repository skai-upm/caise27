## ADDED Requirements

### Requirement: Delivery 3 design in the diagram
`uml/model.puml` SHALL represent the final Delivery 3 design consistently with the code, making identifiable: the ticket access interfaces and their implementation by tickets; the generic attraction type and its GENERAL/INDIVIDUAL/GROUP/SPECIAL subtypes with their bounds; passes and their relationship to individual tickets with cardinalities (0..* SpeedPasses and 0..1 Special Pass per individual ticket); the invoice Builder abstraction, concrete builders and invoice; the singleton controllers; and the dependency-injection relationships established by the composition root.

#### Scenario: Delivery 3 elements present
- **WHEN** the diagram is inspected
- **THEN** it declares `TicketAccess`, `IndividualAccess`, `GroupAccess`, `Attraction<T extends TicketAccess>` and its four subtypes, `SpeedPass` and `SpecialPass` related to `IndividualTicket` with `0..*` and `0..1`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `Invoice`, the controllers marked as singletons, and `ApplicationInitializer` with its injection dependencies
