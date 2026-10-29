# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 class model
`uml/model.puml` SHALL represent the final Delivery 3 design: the access interfaces implemented by tickets, the generic attraction hierarchy and its types, the relationships between individual tickets and passes with their cardinalities (any number of SpeedPasses, at most one Special Pass per ticket), the Builder abstractions for user and group invoices, the dependency-injection composition root and the dependencies it injects, and the singleton controllers.

#### Scenario: Delivery 3 elements present
- **WHEN** `uml/model.puml` is read
- **THEN** it declares `AccessTicket`, `Attraction<T extends AccessTicket>` with its four subclasses, `SpeedPass` and `SpecialPass` linked to `IndividualTicket` with multiplicities `0..*` and `0..1`, `InvoiceBuilder` with its concrete builders, `ParkInitializer`, and the controllers marked `<<singleton>>`, and every declared type exists in the code
