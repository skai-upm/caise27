# Spec Delta

## Purpose

Keeps a PlantUML class model in the repository that documents the application's classes, their relationships and multiplicities as the system evolves.

## ADDED Requirements

### Requirement: PlantUML class model
The repository SHALL contain a PlantUML class diagram at `docs/uml/model.puml` that includes every application class and enum, the associations between them and their multiplicities, and SHALL be updated whenever a change adds or alters classes or relationships.

#### Scenario: Group relationship documented
- **WHEN** the model is inspected after this change
- **THEN** it shows `Group` associated with `User` with multiplicity `0..*` members per group, and the managers and storage classes with their dependencies

#### Scenario: Model is valid PlantUML
- **WHEN** the file is read
- **THEN** it starts with `@startuml` and ends with `@enduml`
