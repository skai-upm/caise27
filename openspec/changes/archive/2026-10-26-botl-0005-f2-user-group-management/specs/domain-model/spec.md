# Spec Delta

## Purpose

Keeps a PlantUML class model of the application's domain in the repository so the object-oriented design can be reviewed alongside the code.

## ADDED Requirements

### Requirement: PlantUML domain model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram of the domain classes and their relationships with direction and multiplicities, updated whenever domain classes or relationships change.

#### Scenario: Model reflects groups
- **WHEN** `uml/model.puml` is processed by PlantUML after this change
- **THEN** it parses without errors and shows `User`, `Group` and the group-to-users association with multiplicity `0..*`
