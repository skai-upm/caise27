# Proposal

## Why

Delivery 3 added passes and attraction types. Their state must survive restarts and `--fly` isolation must cover it; deleting a future ticket must also delete the passes attached to it consistently and atomically; and the UML model must describe the final F3 design.

## What Changes

- Verify and specify persistence of passes (identity, price, associated individual ticket, consumed status) and of attraction types (existing untyped records = GENERAL) across restarts. (Both were introduced by the pass and attraction-type changes; this change makes the restart contract explicit and tests it end to end.)
- Cascade deletion: a valid `TICKET DELETE` also deletes all SpeedPasses and the Special Pass of that ticket; a valid `GROUPTICKET DELETE` deletes the passes of each constituent ticket, then the constituent tickets, then the group ticket.
- Atomicity: a rejected deletion deletes nothing; if storing any part of a cascade fails, the already-written parts are restored so no partial deletion remains.
- `--fly` explicitly covers `passes.txt` and typed attractions.
- `uml/model.puml` checked and completed so it reflects the implemented F3 classes, interfaces, generics and relationships.

The source suggests change ID `extend-f3-persistence-and-uml`-style naming; this batch uses the BOTL ID `botl-0019-19-f3-extended-persistence-and-uml-model`. Mandatory milestone message: `[F3-C09] Extend delivery 3 persistence and UML`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `ticket-passes`: new requirements for cascade deletion with tickets and for the restart contract.
- `fly-mode`: the temporary directory also holds passes.
- `uml-model`: new requirement listing the F3 elements the model must show.

## Impact

- `ParkController.deleteTicket/deleteGroupTicket` coordinate the cascade; `TicketManager` gains pre-validation methods; `PassManager` gains removal/restore of a ticket's passes. Output of a cascading delete also reports the number of passes deleted.
