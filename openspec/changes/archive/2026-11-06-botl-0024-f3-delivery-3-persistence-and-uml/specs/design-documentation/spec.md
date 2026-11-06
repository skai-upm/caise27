# Spec Delta

## ADDED Requirements

### Requirement: Diagram represents the Delivery 3 design
The diagram SHALL make it possible to identify the ticket access interfaces, the generic attraction class and its type-specific subclasses, the relationships between individual tickets and passes with their cardinalities (any number of SpeedPasses and at most one Special Pass per individual ticket), the invoice builder abstraction with its user and group builders, the relationships established by dependency injection, and the controllers designed as single instances.

#### Scenario: Pass cardinalities
- **WHEN** the diagram is inspected
- **THEN** an individual ticket relates to `0..*` SpeedPasses and `0..1` Special Pass

#### Scenario: Delivery 3 elements present
- **WHEN** the diagram is inspected
- **THEN** it shows `AccessTicket`, `IndividualAccess`, `GroupAccess`, `Attraction<T extends AccessTicket>` with its four subclasses, `InvoiceBuilder` with `UserInvoiceBuilder` and `GroupInvoiceBuilder`, relationships labelled as injected, and `ParkController` and `InvoiceController` marked as singletons
