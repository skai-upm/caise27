# Proposal

## Why

Milestone F2-C09 requires the Phase 2 design to be documented as a PlantUML class diagram in `uml/model.puml`. It must show classes, interfaces, inheritance, implementations, associations, compositions, directions and multiplicities, particularly the ticket hierarchy, the group-ticket composition and the relationships between users, groups, tickets and attractions. It must match the submitted code.

## What Changes

- Add `uml/model.puml`, a class diagram of the current implementation's packages (`cli`, `park`, `persistence`) with relevant attributes only, no methods or constructors. It shows:
  - the `Ticket` hierarchy (`IndividualTicket`, `GroupTicket` extend the abstract `Ticket`)
  - composition `GroupTicket *-- "1..*" IndividualTicket`
  - the one-way associations `GroupTicket --> "0..1" Group`, `ParkController` → groups/tickets/attractions, and the repositories → their files
  - interface implementations and dependencies (`UserDirectory`, `GroupTicketDates`, `CommandHandler`)
  - a note explaining that users are currently referenced by e-mail through `UserDirectory`, because the user model is not in the code base yet (batch ordering)
- Add an automated consistency check: every production class or interface is declared in the diagram, and the key inheritance and composition edges exist and match the code.

### Out of scope
- Rendering images, and diagrams for later deliveries.

## Capabilities

### New Capabilities
- `design-documentation`: The PlantUML class model kept in the repository and its consistency with the implemented design.

### Modified Capabilities
<!-- None -->

## Impact

- New file `uml/model.puml`.
- New test `UmlModelTest`.
