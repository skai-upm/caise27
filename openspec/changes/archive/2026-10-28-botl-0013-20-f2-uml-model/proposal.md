# Proposal

## Why

Milestone F2-C09 requires the Phase 2 design to be documented as a PlantUML class diagram in `uml/model.puml`, showing the model structure and relationships (ticket hierarchy, composition of a group ticket and its individual tickets, relationships among users, groups, tickets and attractions) with directions and multiplicities, and corresponding to the code actually implemented.

## What Changes

- Add `uml/model.puml` describing the domain model classes with their relevant attributes (no methods/constructors), the abstract `Ticket` hierarchy, the `GroupTicket` → `IndividualTicket` composition, unidirectional associations with multiplicities (ticket → user, group ticket → group 0..1, group → members, attraction → occupants) and the single `ParkController` that holds the park elements.
- Add an automated consistency check between the diagram and the model classes so the diagram keeps matching the code.
- Commit with the exact message `[F2-C09] Add PlantUML class model`.

## Capabilities

### New Capabilities
- `design-documentation`: The UML class model delivered with the code and its consistency with the implementation.

### Modified Capabilities
<!-- none -->

## Impact

- New `uml/model.puml` and a test; no production code changes.
- **Flagged ambiguities:**
  - The source asks for the "model structure"; command, persistence and entry-point classes are not drawn; the central `ParkController` is included because it owns the users, groups, tickets and attractions.
  - The project has no enumerations or interfaces in the model at this point, so none are drawn.
  - The batch order applies this milestone before the F1 user model milestones; the diagram reflects the code as it is now (user with DNI and e-mail) and later model changes must update it.
