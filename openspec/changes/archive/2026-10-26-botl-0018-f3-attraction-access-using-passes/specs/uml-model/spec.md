# Spec Delta

## ADDED Requirements

### Requirement: Model shows pass-based access
`uml/model.puml` SHALL show the attraction controller's association with the pass controller used to persist consumed passes.

#### Scenario: Collaboration in the diagram
- **WHEN** `uml/model.puml` is inspected after pass-based access is implemented
- **THEN** it shows `AttractionController` associated with `PassController`
