## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the model showing classes, interfaces, relevant enumerations, inheritance, associations, compositions, directions and multiplicities, including the ticket hierarchy, the composition between group tickets and their individual tickets, the relationships among users, groups, tickets and attractions, the ticket access interfaces, generic attraction types, passes and their cardinalities, the invoice builder abstractions, singleton controllers and injected dependencies. It SHALL match the implemented classes.

#### Scenario: Diagram present and consistent
- **WHEN** `uml/model.puml` is inspected
- **THEN** every model class it names exists in the code with the shown relationships
