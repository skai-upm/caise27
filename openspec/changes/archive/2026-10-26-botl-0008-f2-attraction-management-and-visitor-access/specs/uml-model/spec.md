# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model file
The repository SHALL contain a valid PlantUML class diagram at `uml/model.puml` that represents the implemented classes, enumerations, relationships and multiplicities.

#### Scenario: Model includes groups
- **WHEN** `uml/model.puml` is inspected after groups are implemented
- **THEN** it shows the group class, its association to users with multiplicity `0..*`, and the classes that manage and persist groups

#### Scenario: Model includes tickets
- **WHEN** `uml/model.puml` is inspected after tickets are implemented
- **THEN** it shows the ticket class with its reference to exactly one user, and the classes that manage and persist tickets

#### Scenario: Model includes the ticket hierarchy
- **WHEN** `uml/model.puml` is inspected after group tickets are implemented
- **THEN** it shows the abstract ticket with its individual and group subclasses, the aggregation from group ticket to group, and the composition from group ticket to its individual tickets with multiplicities

#### Scenario: Model includes attractions
- **WHEN** `uml/model.puml` is inspected after attractions are implemented
- **THEN** it shows the attraction class with its collection of present users (`0..*`) and the classes that manage and persist attractions
