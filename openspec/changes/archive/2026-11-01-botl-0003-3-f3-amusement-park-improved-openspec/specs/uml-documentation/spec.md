# Spec Delta

## ADDED Requirements

### Requirement: Diagram covers Delivery 3 design
`uml/model.puml` SHALL represent the Delivery 3 implementation, including the layer packages, passes, invoices and their builder, access-capability interfaces and their implementations, the generic attraction hierarchy with its type bounds, and the attraction-type enumeration.

#### Scenario: Generics and interfaces shown
- **WHEN** the diagram is compared with the final code
- **THEN** each access interface, generic attraction class with its bound, pass class and builder appears with the relationships present in the code
