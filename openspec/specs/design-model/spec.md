# design-model Specification

## Purpose

Documents the implemented model as a PlantUML class diagram kept consistent with the code.

## Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the model showing classes, interfaces, relevant enumerations, inheritance, associations, compositions, directions and multiplicities, including the ticket hierarchy, the composition between group tickets and their individual tickets, and the relationships among users, groups, tickets and attractions. It SHALL match the implemented classes.

#### Scenario: Diagram present and consistent
- **WHEN** `uml/model.puml` is inspected
- **THEN** every model class it names exists in the code with the shown relationships
