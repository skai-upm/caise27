# Spec Delta

## ADDED Requirements

### Requirement: Model reflects the Delivery 3 design
`uml/model.puml` SHALL contain every class, interface and enumeration of the main code, every field typed with a model class as a named relationship, the generic attraction hierarchy and the pass composition, and nothing that the code does not contain.

#### Scenario: Complete cross-check
- **WHEN** the model is cross-checked against the source fields
- **THEN** no class, attribute or relationship is missing or invented
