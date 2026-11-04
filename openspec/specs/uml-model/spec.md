# uml-model Specification

## Purpose
Keeps a PlantUML class model in the repository that documents the application's classes, their relationships and multiplicities, consistent with the implemented code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `docs/uml/model.puml` that represents the application's classes and enums with their associations and multiplicities, including the association between groups and their member users.

#### Scenario: Group relationship present
- **WHEN** `docs/uml/model.puml` is reviewed
- **THEN** it is a valid `@startuml`/`@enduml` document declaring `Group` and `User` with a `Group` to `User` association of multiplicity `0..*` members, and each class it declares exists in the code
