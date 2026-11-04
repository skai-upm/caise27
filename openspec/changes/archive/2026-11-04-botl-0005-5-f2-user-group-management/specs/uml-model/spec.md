# Spec Delta

## Purpose

Keeps a PlantUML class model in the repository that documents the application's classes, relationships and multiplicities as they are implemented.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a valid PlantUML class diagram representing the application's current classes, including `Group` and its relationships with `User` and the classes that manage and persist groups, with multiplicities.

#### Scenario: Model is valid and current
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** it parses without errors and contains `Group`, `GroupManager`, `GroupFileStore` and a `Group`–`User` association with multiplicities on both ends
