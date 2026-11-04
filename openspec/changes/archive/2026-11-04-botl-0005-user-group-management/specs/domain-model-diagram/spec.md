# Spec Delta

## Purpose

Keeps a PlantUML class model in the repository that represents the application's classes, relationships and multiplicities as they currently are.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `docs/uml/model.puml` that represents every production class and enum, their relationships, and the multiplicities of their associations, consistent with the current source code.

#### Scenario: Diagram covers groups
- **WHEN** `docs/uml/model.puml` is reviewed after this change
- **THEN** it contains `User`, `Group`, `GroupManager`, `UserManager` and the other production classes, a `Group` to `User` association with multiplicities `0..*` on both ends, and a `GroupManager` to `Group` composition with `0..*` groups

#### Scenario: Diagram is valid PlantUML
- **WHEN** the file is read
- **THEN** it starts with `@startuml` and ends with `@enduml`
