# Spec Delta

## ADDED Requirements

### Requirement: Invoice builder in the diagram
The class diagram SHALL represent the invoice Builder: the `InvoiceBuilder` abstraction, its concrete builders, the director and the built `Invoice` with its sections and lines.

#### Scenario: Builder hierarchy shown
- **WHEN** the diagram is reviewed
- **THEN** it shows `UserInvoiceBuilder` and `GroupInvoiceBuilder` realising `InvoiceBuilder` through `AbstractInvoiceBuilder`, and `Invoice` composed of `InvoiceSection`s composed of `InvoiceLine`s
