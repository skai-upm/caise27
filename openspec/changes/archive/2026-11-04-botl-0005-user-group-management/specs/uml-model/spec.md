# Spec Delta

## Purpose

Keeps a PlantUML class model of the application in the repository so the classes, their relationships and multiplicities are documented as the project evolves.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `uml/model.puml` that represents the application's current classes and enumerations together with their relationships and multiplicities.

#### Scenario: Model contains the group relationship
- **WHEN** `uml/model.puml` is inspected after this change
- **THEN** it declares `User`, `UserField`, `Group` and the services, repositories and controller, and shows the association from `Group` to `User` with multiplicity `0..*` members and a user belonging to `0..*` groups

#### Scenario: Model is valid PlantUML
- **WHEN** the file is read
- **THEN** it is enclosed in `@startuml` / `@enduml` and every class referenced by a relationship is declared
