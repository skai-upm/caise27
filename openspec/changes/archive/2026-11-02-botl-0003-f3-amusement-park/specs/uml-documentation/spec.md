# Spec Delta

## MODIFIED Requirements

### Requirement: Diagram reflects the implementation
The diagram SHALL show the project's relevant classes, interfaces (including ticket access capabilities), abstract classes, generic type parameters with their bounds, and enumerations, organized by layer package, with their real inheritance/implementation relationships and navigable associations and compositions, with direction and multiplicities inferable from the code. It SHALL NOT contain elements or reverse navigations absent from the code, and functional code SHALL NOT be changed to suit the diagram.

#### Scenario: Ticket hierarchy and relationships
- **WHEN** the diagram is compared with the source code
- **THEN** every class, generalization, association, and composition in the diagram corresponds to a type or field in the code, including the ticket hierarchy and the user, group, ticket, and attraction references

#### Scenario: F3 elements
- **WHEN** the diagram is compared with the F3 code
- **THEN** it shows the access interfaces, the generic attraction hierarchy with its bounds, the pass hierarchy, the invoice builder, and the repository interfaces with their implementations
