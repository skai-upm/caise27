# Proposal

## Why

Delivery 3 state (passes, consumption, attraction types) must survive restarts, and deleting a future ticket must clean up the passes it owns. Today passes and types are already persisted, but ticket deletion is blocked while passes exist (an interim guard) instead of cascading.

## What Changes

- Replace the interim guard with cascade deletion: valid `TICKET DELETE` removes the ticket's passes then the ticket; valid `GROUPTICKET DELETE` removes member passes, then member tickets, then the group ticket.
- Deletions are validated completely before anything is removed; if a later save fails, removed passes are restored (no partial deletion).
- Confirm durable reconstruction of passes (identity, price, ticket link, consumed status) and attraction types (legacy lines as GENERAL), and that `--fly` isolates the passes file too; strengthen tests for all of these.
- Re-check `uml/model.puml` against the implementation (classes, interfaces, generics, associations).

## Capabilities

### New Capabilities
- `ticket-cascade-deletion`: individual and group cascades and the no-partial-deletion rule.

### Modified Capabilities
- `passes`: the "Tickets with passes are not deleted alone" requirement is removed (replaced by cascades).
- `temporary-persistence-mode`: the `--fly` directory explicitly covers the passes file.

## Impact

- `TicketManager` gains non-mutating deletion checks; `PassManager` gains `removeAll`/`restore`; `ParkController.deleteTicket`/`deleteGroupTicket` orchestrate the cascades.
- No file-format changes.
- Delivery milestone message: `[F3-C09] Extend delivery 3 persistence and UML`.
