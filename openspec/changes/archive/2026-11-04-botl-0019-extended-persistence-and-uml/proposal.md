# Proposal

## Why

Delivery 3 added passes and attraction types; their state must survive restarts, ticket deletions must clean up owned passes in a defined order without ever leaving a partial deletion, `--fly` must isolate the new data too, and the UML model must reflect the F3 design. Most persistence already exists (`passes.txt`, attraction type field); the remaining gap is the cascade order and failure safety: today tickets are removed before their passes and a failure between the two writes would leave a partial deletion.

## What Changes

- Cascade order as required by the source: for `TICKET DELETE`, the ticket's SpeedPasses and Special Pass first, then the ticket; for `GROUPTICKET DELETE`, the passes of every constituent first, then the constituent tickets, then the group ticket.
- Failure safety: the deletion is fully validated before anything is removed (existence, strictly future date, not a constituent for `TICKET DELETE`); if a later write fails, the passes already removed are restored, so no partial cascade remains.
- Persistence of passes (identity, stored price, ticket, consumed status) and of attraction types (legacy 3-field lines read as GENERAL) is confirmed by restart tests in normal and `--fly` mode; `--fly` keeps all F3 files (`passes.txt`, typed `attractions.txt`) in the temporary directory.
- `uml/model.puml` checked to represent the implemented F3 classes, interfaces, generics and relationships (verified against the code by `UmlModelTest`).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `passes`: "Ticket deletion removes its passes" — deletion order and no partial cascade.
- `temporary-persistence-mode`: "All persistence redirected" — explicitly covers passes and attraction types.
- `uml-model`: "PlantUML class model" — scenario for the F3 elements (pass hierarchy, capability interfaces, generic attractions).

## Impact

- **Code**: `TicketService` (side-effect-free `checkDeletable` / `checkGroupDeletable`), `PassService` (`removePassesOf` returns the removed passes; `restore`), `ParkController.deleteTicket/deleteGroupTicket` (validate → passes → tickets, with restore on failure). No format changes.
- **Mandatory milestone message**: `[F3-C09] Extend delivery 3 persistence and UML`.
- **Assumption**: "processing failure" means a failed file write; the restore re-writes the previous pass state.
