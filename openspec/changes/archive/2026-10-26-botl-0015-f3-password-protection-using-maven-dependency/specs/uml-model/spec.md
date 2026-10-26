# Spec Delta

## ADDED Requirements

### Requirement: Model shows password encoding
`uml/model.puml` SHALL show the password encoder abstraction, its BCrypt implementation and the user controller's association with the abstraction.

#### Scenario: Encoder in the diagram
- **WHEN** `uml/model.puml` is inspected after password hashing is introduced
- **THEN** it shows the encoder interface realised by the BCrypt encoder and associated from the user controller
