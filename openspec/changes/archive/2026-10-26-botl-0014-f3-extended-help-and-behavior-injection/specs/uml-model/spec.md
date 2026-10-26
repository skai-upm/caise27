# Spec Delta

## ADDED Requirements

### Requirement: Model shows the help structure
`uml/model.puml` SHALL show the help generator, the help format interface with its implementations, and the command catalog with its command descriptors.

#### Scenario: Help strategy in the diagram
- **WHEN** `uml/model.puml` is inspected after extended help is implemented
- **THEN** it shows the help format interface realised by the concise and extended formats, and the help generator associated with one format and one catalog
