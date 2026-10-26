# Spec Delta

## Purpose

Defines the PlantUML class model of the application kept in `uml/model.puml`, which documents the implemented classes, their relationships and multiplicities and must stay consistent with the code.

## ADDED Requirements

### Requirement: PlantUML class model
The project SHALL contain a valid PlantUML class diagram at `uml/model.puml` that represents the implemented domain classes, enumerations and main application classes, with their associations, navigability and multiplicities, and SHALL be updated whenever classes or relationships change.

#### Scenario: Model file is valid
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it is accepted without syntax errors

#### Scenario: Groups represented
- **WHEN** the model is inspected after group management is implemented
- **THEN** it shows `Group` with its association to `User` (a group has zero or more members) and the classes that manage and persist groups
