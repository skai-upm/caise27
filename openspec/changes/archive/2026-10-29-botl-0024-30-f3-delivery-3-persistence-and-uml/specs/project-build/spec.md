# Spec Delta

## ADDED Requirements

### Requirement: UML shows the Delivery 3 design
`uml/model.puml` SHALL make it possible to identify the access interfaces used by tickets, the use of generics in attractions, the attraction types, the relationships between individual tickets and passes with their cardinalities (any number of SpeedPasses and at most one Special Pass per individual ticket), the Builder abstractions for user and group invoices, the relationships resulting from dependency injection, and the controllers whose uniqueness is part of the design, consistently with the code.

#### Scenario: Delivery 3 checklist present
- **WHEN** the build runs its tests
- **THEN** the diagram declares `AccessTicket`, `IndividualAccess`, `GroupAccess`, a generic `Attraction<T extends AccessTicket>` with its four subclasses and `AttractionType`, `SpeedPass "0..*"` and `SpecialPass "0..1"` towards `IndividualTicket`, `InvoiceBuilder` with `UserInvoiceBuilder` and `GroupInvoiceBuilder`, injected relationships, and `<<singleton>>` controllers
