# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 design represented
`uml/model.puml` SHALL represent the final Delivery 3 design: the access interfaces implemented by tickets, the generic attraction hierarchy and attraction types, the relationships between individual tickets and passes with their cardinalities (0..* SpeedPasses, 0..1 Special Pass per ticket), the Builder abstractions and director used to build user and group invoices, the singleton controllers and the dependencies injected at initialization.

#### Scenario: Diagram content
- **WHEN** the diagram is inspected
- **THEN** it contains the access interfaces, `Attraction<T extends AttractionAccess>` with its four subclasses, `SpeedPass`/`SpecialPass` linked to `IndividualTicket` with `0..*` and `0..1`, `InvoiceBuilder`/`StandardInvoiceBuilder`/`InvoiceController`, the `<<singleton>>` controllers and `ApplicationInitializer` injections

#### Scenario: Consistency check covers controllers and invoices
- **WHEN** the test suite runs
- **THEN** every class of the model, controller and invoice packages appears in the diagram and every attribute shown exists in the code
