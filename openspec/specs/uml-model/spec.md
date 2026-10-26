# uml-model Specification

## Purpose
Keeps a PlantUML class model of the implemented object-oriented design in the repository, consistent with the code.

## Requirements

### Requirement: PlantUML class model file
The repository SHALL contain a valid PlantUML class diagram at `uml/model.puml` that represents the implemented classes, enumerations, relationships and multiplicities.

#### Scenario: Model includes groups
- **WHEN** `uml/model.puml` is inspected after groups are implemented
- **THEN** it shows the group class, its association to users with multiplicity `0..*`, and the classes that manage and persist groups

#### Scenario: Model includes tickets
- **WHEN** `uml/model.puml` is inspected after tickets are implemented
- **THEN** it shows the ticket class with its reference to exactly one user, and the classes that manage and persist tickets
