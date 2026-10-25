# Spec Delta

## Purpose

Keeps a PlantUML class model of the application in the repository that documents the classes, relationships and multiplicities actually present in the code.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram of the application's current classes and enumerations, with their associations, directions and multiplicities, consistent with the code. It SHALL be updated whenever classes or relationships are added.

#### Scenario: Model is valid PlantUML
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted without syntax errors

#### Scenario: Group relationship represented
- **WHEN** the model is inspected after groups are introduced
- **THEN** it shows `Group` with an association to `User` with multiplicity `0..*` on the member side, held by the group
