# Spec Delta

## ADDED Requirements

### Requirement: Interfaces and generics in the model
The diagram SHALL represent the interfaces implemented by the domain classes, the generic attraction type with its bound and the concrete bindings of its subclasses, together with the pass hierarchy and invoice builder types present in the code.

#### Scenario: Generic attraction
- **WHEN** attractions are declared as a generic class bounded by an access-capability interface
- **THEN** the diagram shows the generic parameter with its bound and each attraction subclass bound to its concrete access interface
