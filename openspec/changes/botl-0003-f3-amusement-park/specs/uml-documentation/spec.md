# Spec Delta

## MODIFIED Requirements

### Requirement: Diagram reflects the implementation
The diagram SHALL show the relevant classes, interfaces, abstract classes, generic type parameters and their bounds, and enumerations, grouped by their layer packages, the actual inheritance and implementation relationships, and the associations, aggregations, and compositions that exist as fields, with navigability direction and multiplicities inferable from the code. It SHALL NOT contain elements or reverse navigations that do not exist in the code, and producing it SHALL NOT change functional code.

#### Scenario: Diagram matches implementation
- **GIVEN** the final F3 implementation
- **WHEN** `uml/model.puml` is compared with the source code
- **THEN** every class, interface, generic bound, relationship, and direction shown exists in the code, and the ticket and pass hierarchies and the relationships among users, groups, tickets, passes, and attractions match the fields actually present
