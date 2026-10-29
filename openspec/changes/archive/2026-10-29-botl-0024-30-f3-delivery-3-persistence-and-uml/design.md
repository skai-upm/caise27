# Design

## Context

Passes (`TextFilePassRepository`), attraction types (`TextFileAttractionRepository`) and cascades exist from earlier Delivery 3 changes; `App.run` wires repositories for normal and `--fly` directories.

## Goals / Non-Goals

**Goals:** prove persistence end to end; complete the diagram. **Non-Goals:** new behavior.

## Decisions

- **`Delivery3PersistenceTest`** drives `App.run` with stdin scripts. Because users can only be registered programmatically in this batch (USER CREATE comes later), the test seeds a data directory through the repositories (users, group, tickets) and then runs command scripts through `App.run` that buy passes, enter attractions with `SPEED` and Special Passes, and delete a ticket; a second run checks the reloaded state via `ParkController` queries.
- **UML**: add `IndividualTicket "1" <-- "0..*" SpeedPass` and `IndividualTicket "1" <-- "0..1" SpecialPass` lines, keep `Pass --> "1" IndividualTicket`; the checklist test searches the declarations listed in the spec.

## Risks / Trade-offs

- [Seeding through repositories] → uses the same production repositories as the application.
