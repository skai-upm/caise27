# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 elements represented
`uml/model.puml` SHALL represent the implemented Delivery 3 design: the layered packages, the singleton controllers and `ApplicationContext`, the repository interfaces and their file implementations, `PasswordHasher`, the help classes and `HelpFormatter`, the invoice builder hierarchy, the pass hierarchy and its association to individual tickets, `AccessCredential` and the generic `Attraction<T>` with its bound subclasses and `AttractionType`.

#### Scenario: F3 elements present and consistent
- **WHEN** `uml/model.puml` is checked with PlantUML and compared with the sources
- **THEN** it parses, every listed element appears, and every relationship corresponds to an attribute or inheritance that exists in the code
