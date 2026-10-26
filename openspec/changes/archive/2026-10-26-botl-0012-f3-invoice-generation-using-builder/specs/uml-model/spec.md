# Spec Delta

## ADDED Requirements

### Requirement: Model shows the invoice Builder
`uml/model.puml` SHALL show the invoice builder interface, its implementation, the director and the invoice product with their relationships.

#### Scenario: Builder in the diagram
- **WHEN** `uml/model.puml` is inspected after invoices are implemented
- **THEN** it shows the builder interface implemented by the concrete builder, the director, the invoice with its sections and lines, and the controller that uses them
