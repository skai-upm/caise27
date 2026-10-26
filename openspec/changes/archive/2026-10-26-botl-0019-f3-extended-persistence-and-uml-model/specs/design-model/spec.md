# Spec Delta

## ADDED Requirements

### Requirement: Model of the Delivery 3 design
`uml/model.puml` SHALL represent the Delivery 3 design as implemented: the layered packages, the interfaces and their realisations (repositories, builders, help formats, admission capabilities, password hasher), the generic `Attraction<T extends Admission>` with its bound subclasses, the pass hierarchy composed in individual tickets, and the associations among controllers, repositories and the model.

#### Scenario: Model matches the code
- **WHEN** the model consistency test runs against the production classes
- **THEN** every class, interface, enumeration, attribute, association, inheritance and realisation in the model exists in the code and every production type appears in the model
