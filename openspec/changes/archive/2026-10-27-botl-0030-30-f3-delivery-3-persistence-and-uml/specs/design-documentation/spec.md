# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 design in the model
`uml/model.puml` SHALL represent the final Delivery 3 design: the ticket access interfaces and their implementations, the generic attraction and its bounded subclasses for each attraction type, the relationships between individual tickets and passes with SpeedPass (`0..*`) and Special Pass (`0..1`) cardinalities, the Builder abstractions for user and group invoices, the relationships created by dependency injection, and the singleton controllers.

#### Scenario: Delivery 3 elements present
- **WHEN** the automated consistency check reads the model
- **THEN** it finds the access interfaces with their realisations, `Attraction<T extends AccessTicket>` with its four subclasses, SpeedPass and Special Pass associations to `IndividualTicket` with multiplicities `0..*` and `0..1`, `InvoiceBuilder` with `UserInvoiceBuilder` and `GroupInvoiceBuilder`, injected repository associations of the services, and the `<<singleton>>` controllers
