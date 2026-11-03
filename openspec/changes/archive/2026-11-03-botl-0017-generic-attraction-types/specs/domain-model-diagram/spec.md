# Spec Delta

## ADDED Requirements

### Requirement: Attraction generics in the diagram
The class diagram SHALL show the access-capability interfaces realised by the ticket classes and the generic `Attraction<T extends AccessTicket>` with its four subclasses and their type bindings.

#### Scenario: Generics shown
- **WHEN** the diagram is reviewed
- **THEN** `Ticket` realises `IndividualAccess`, `GroupTicket` realises `GroupAccess`, both extend `AccessTicket`, and `GroupAttraction` specialises `Attraction` bound to `GroupAccess`
