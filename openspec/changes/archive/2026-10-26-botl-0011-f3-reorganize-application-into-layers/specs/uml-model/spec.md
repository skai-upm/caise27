# Spec Delta

## ADDED Requirements

### Requirement: Model shows layer packages
`uml/model.puml` SHALL group classes in the same packages as the code (presentation, business model, business controllers, business validation, persistence).

#### Scenario: Packages in the diagram
- **WHEN** `uml/model.puml` is inspected after the layered reorganisation
- **THEN** each class appears inside the package that contains it in the code
