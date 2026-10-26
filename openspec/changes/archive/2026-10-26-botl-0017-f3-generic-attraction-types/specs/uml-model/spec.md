# Spec Delta

## ADDED Requirements

### Requirement: Model shows generic attractions
`uml/model.puml` SHALL show the access capability interfaces implemented by the ticket classes, the generic attraction class with its type parameter bound, its typed subclasses and the attraction type enumeration.

#### Scenario: Generic hierarchy in the diagram
- **WHEN** `uml/model.puml` is inspected after attraction types are implemented
- **THEN** it shows `Attraction<T extends AttractionAccess>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction` and `SpecialAttraction`, and the tickets realising `IndividualAccess` and `GroupAccess`
