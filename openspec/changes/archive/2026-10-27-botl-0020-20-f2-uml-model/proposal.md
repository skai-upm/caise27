# Proposal

## Why

Milestone F2-C09 requires the Delivery 2 design to be documented as a PlantUML class diagram, stored at `uml/model.puml`, consistent with the submitted code.

## What Changes

- Add `uml/model.puml` describing the model: `User`, `Group`, `Attraction`, the ticket hierarchy (`Ticket` abstract, `IndividualTicket`, `GroupTicket`), the `UserField` enumeration, the central `ParkController` singleton and its repositories.
- Show relevant attributes only (no methods/constructors), inheritance, compositions (group ticket ◆→ individual tickets), associations with direction and multiplicities: ticket → user, group ticket → group (0..1), group → users, attraction → users, controller → repositories.
- Add an automated consistency check between the diagram and the code.
- Commit with the exact message `[F2-C09] Add PlantUML class model`.

## Capabilities

### New Capabilities
- `design-model`: The PlantUML class model of the application and its consistency with the code.

### Modified Capabilities
<!-- none -->

## Impact

- New `uml/model.puml` and `UmlModelTest`. No production code changes.
- Ambiguity flagged: persistence and command classes are infrastructure, not "model structure"; they are summarised as packages without attributes to keep the diagram focused while still matching the code.
