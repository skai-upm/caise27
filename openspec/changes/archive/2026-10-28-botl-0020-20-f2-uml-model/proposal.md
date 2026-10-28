# Proposal

## Why

Milestone F2-C09 requires documenting the final Delivery 2 design as a PlantUML class diagram, stored in the repository as `uml/model.puml`, that matches the submitted code — in particular the ticket hierarchy, the composition between group tickets and their individual tickets, and the relationships among users, groups, tickets and attractions.

## What Changes

- Add `uml/model.puml` (PlantUML class diagram) covering the model and its management structure: classes, interfaces, the `UserField` enumeration, inheritance (`Ticket` ← `IndividualTicket`, `GroupTicket`), interface implementations (`UserFileStorage` → `UserStorage`), associations with direction and multiplicity (ticket → user, group ticket → group, attraction → occupants, group → members), and compositions (group ticket ◆ individual tickets, park controller ◆ services/repositories).
- Show only relevant attributes; no methods or constructors.
- Add an automated consistency check between the diagram and the code (every model class/enum/interface in the diagram exists in the code and vice versa).
- Commit with the mandatory message `[F2-C09] Add PlantUML class model`.

## Capabilities

### New Capabilities
- `design-documentation`: The PlantUML class model of the application and its consistency with the code.

### Modified Capabilities
- None.

## Impact

- New `uml/model.puml`, new test `UmlModelConsistencyTest`. No behavior change.
- Ambiguity flagged: groups reference their members by DNI (not object references); the diagram shows it as a directed `members` association annotated "by DNI".
