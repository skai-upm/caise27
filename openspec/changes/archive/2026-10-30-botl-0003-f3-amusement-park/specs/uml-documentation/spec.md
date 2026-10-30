# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class diagram of the implementation
The project SHALL contain `uml/model.puml`, a valid PlantUML class diagram derived from the implemented code, showing the relevant packages, classes, interfaces/abstract classes, generic type parameters with their bounds, and enums, inheritance and interface realization, associations and compositions with their navigability and inferable multiplicities. It SHALL NOT contain elements absent from the code nor reverse navigation that the code lacks.

#### Scenario: Diagram renders
- **WHEN** `uml/model.puml` is processed by PlantUML
- **THEN** it renders without syntax errors

#### Scenario: Diagram matches the code
- **WHEN** the diagram is compared with the source
- **THEN** every class, relationship, direction and multiplicity shown exists in the code, including the ticket hierarchy and the references among users, groups, tickets and attractions

#### Scenario: Delivery 3 elements shown
- **WHEN** the diagram is inspected
- **THEN** it shows the ticket access interfaces, the generic attraction hierarchy with its bounds, the pass classes, the invoice builder and the layered packages
