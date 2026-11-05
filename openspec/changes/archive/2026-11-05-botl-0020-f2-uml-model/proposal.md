# Proposal

## Why

Milestone F2-C09 (Delivery 2, step 9) requires the final Phase 2 design to be documented as a PlantUML class diagram in `uml/model.puml` (source §14). The diagram must show classes, interfaces, relevant enumerations, inheritance, associations, compositions, relationship directions and multiplicities. In particular it must cover the ticket hierarchy, the composition of a group ticket from its individual tickets, and the relationships among users, groups, tickets and attractions. It must correspond to the submitted code. The repository has no UML model today.

## What Changes

- Add `uml/model.puml` describing the model (`User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`), the central management (`ParkController`) and its repositories, and the persistence classes. Only relevant attributes are shown, without methods or constructors.
- Represent the relationship directions implemented in code: ticket → user, group ticket → group (optional), attraction → occupants, group → members. Users never reference tickets, groups or attractions.
- Represent the composition between `GroupTicket` and its `IndividualTicket`s and the `Ticket` inheritance hierarchy, with multiplicities.
- Add an automated consistency test: the diagram must name every model, repository, park-management and persistence class that exists in the code, and contain the key relationships. The diagram is also checked for valid PlantUML syntax with the PlantUML tool.

In scope: the class diagram and its consistency check.

Out of scope: behavior changes (none) and sequence or other diagrams.

## Capabilities

### New Capabilities
- `design-documentation`: The PlantUML class model of the application, its required content, and its consistency with the implementation.

### Modified Capabilities
- None.

## Impact

- New `uml/model.puml` and a `UmlModelConsistencyTest`. No production code changes.
