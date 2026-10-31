# Spec Delta

## MODIFIED Requirements

### Requirement: Diagram reflects the implementation
The diagram SHALL show the relevant domain and architecture classes, interfaces and enumerations with their actual inheritance and implementation relationships, generic type parameters and bounds, and associations, aggregations and compositions with the navigability and multiplicities present in the code, grouped by their actual packages. It SHALL NOT contain elements or reverse navigations that are not implemented.

#### Scenario: Ticket hierarchy and relationships
- **WHEN** the diagram is compared with the code
- **THEN** the ticket hierarchy, and the references among users, groups, tickets and attractions, match the fields actually declared in the classes

#### Scenario: Delivery 3 elements
- **WHEN** the diagram is compared with the F3 code
- **THEN** passes, access interfaces, generic attraction classes with their bounds, invoice builder classes, repository interfaces and their implementations appear as implemented
