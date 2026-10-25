# Spec Delta

## MODIFIED Requirements

### Requirement: Diagram reflects the current code
The diagram SHALL show the relevant classes, interfaces and enumerations that exist in the code, including the implemented ticket and pass hierarchies, the attraction type hierarchy with its generic parameters, the access-capability interface, the invoice builder, and the controller and repository structure, and SHALL NOT invent elements that are not implemented. Producing it SHALL NOT change application behavior.

#### Scenario: Ticket hierarchy
- **WHEN** the diagram is compared with the code
- **THEN** the ticket abstraction and its concrete ticket classes appear with the same inheritance as in the code

#### Scenario: Generics and interfaces
- **WHEN** the diagram is compared with the code
- **THEN** the generic attraction class shows its bounded type parameter, each concrete attraction class shows its binding, and interface implementations appear as realisations
