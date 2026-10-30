# Spec Delta

## MODIFIED Requirements

### Requirement: PlantUML class model
The repository SHALL contain `uml/model.puml`, a PlantUML class diagram of the final model with relevant attributes, classes, interfaces, enumerations, inheritance, interface implementations, associations, compositions, directions and multiplicities, including the ticket hierarchy and access interfaces, group ticket composition, the generic attraction types, passes and their cardinalities, the invoice builder classes, the Singleton controllers and the injected dependencies.

#### Scenario: Diagram matches the code
- **WHEN** `uml/model.puml` is compared with the model classes in the source code
- **THEN** every class, enumeration and relationship it shows exists in the code with the same name, direction and multiplicity
