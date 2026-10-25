# Spec Delta

## ADDED Requirements

### Requirement: E3 design documented
`uml/model.puml` SHALL reflect the Delivery 3 design: layer packages, controllers, repositories, passes, invoice builder, access interfaces and their implementations, and the generic attraction hierarchy with the bound types of each attraction subtype.

#### Scenario: Generics and interfaces shown
- **WHEN** the diagram is compared with the source
- **THEN** every access interface, the generic attraction base class and the four attraction subclasses appear with their implementation and inheritance relationships
