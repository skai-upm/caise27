# Proposal

## Why

Delivery 3 state must be fully persistent and consistent: passes (with prices, tickets and consumed status) and attraction types must survive restarts — already stored by the passes and attraction-type changes — and deleting future tickets must cascade to their passes instead of being blocked, without ever leaving partial deletions. The PlantUML model must reflect the final Delivery 3 design.

## What Changes

- `TICKET DELETE` of a valid future individual ticket cascade-deletes its SpeedPasses and Special Pass (replacing the interim rule that blocked deleting tickets with passes).
- `GROUPTICKET DELETE` of a valid future group ticket deletes, in order, the passes of each internal ticket, then those internal tickets, then the group ticket.
- Deletions are atomic: a rejected deletion changes nothing, and if storing the result fails the in-memory state is restored.
- Persistence of passes and attraction types is verified end-to-end, including `--fly` runs.
- `uml/model.puml` is reconciled with the complete E3 code (classes, interfaces, generics, associations).

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `tickets`: ticket deletion cascades to passes; the "tickets with passes are not deleted" rule is removed; deletions are atomic.
- `group-tickets`: group-ticket deletion cascades to passes and internal tickets in order and is atomic.
- `passes`: pass persistence also covers cascade deletions and `--fly` runs.
- `uml-model`: the model reflects the full E3 design.

## Impact

- `TicketController` (cascade + rollback, deletion listeners), `PassController` (persists after cascades), tests, `uml/model.puml`. No command syntax change.
