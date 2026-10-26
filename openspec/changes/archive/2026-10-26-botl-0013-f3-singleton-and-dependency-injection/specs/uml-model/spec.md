# Spec Delta

## ADDED Requirements

### Requirement: Model shows singletons and repository abstractions
`uml/model.puml` SHALL mark Singleton controllers and SHALL show the repository interfaces with their persistence implementations, with controllers associated to the interfaces.

#### Scenario: Repository realisation
- **WHEN** `uml/model.puml` is inspected after dependency injection is introduced
- **THEN** each file repository realises its business repository interface and each controller is associated with the interface, and controllers are stereotyped `<<Singleton>>`
