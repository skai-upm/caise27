# domain-model-diagram Specification

## Purpose

Documents the implemented object-oriented design as a PlantUML class diagram kept with the source code.

## Requirements

### Requirement: PlantUML class model
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram that represents the implemented classes and enums, their relationships and the multiplicities of the associations (including group membership of users and the user owning each ticket).

#### Scenario: Diagram is valid
- **WHEN** `uml/model.puml` is checked with PlantUML
- **THEN** it is accepted without syntax errors

#### Scenario: Group relationships represented
- **WHEN** the diagram is reviewed
- **THEN** it shows `Group` associated with `User` with multiplicity `*` members, alongside the existing user, storage, service and command classes

#### Scenario: Ticket relationships represented
- **WHEN** the diagram is reviewed
- **THEN** it shows `Ticket` referencing its owning `User` with multiplicity `0..1` (absent only for historical tickets of deleted users), and the ticket service and storage classes
