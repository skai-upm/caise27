# Proposal

## Why

Delivery 3 introduced passes and attraction types; the persistence layer and the class model must now cover that state completely. Deleting future tickets must also remove the passes that depend on them in a consistent order, and a failed deletion must never leave the data half deleted.

## What Changes

- Persistence reconstructs every pass with its price, its individual ticket and its consumption status, and every attraction with its type, across executions (normal and `--fly`).
- `TICKET DELETE` of a future individual ticket cascade-deletes its SpeedPasses and Special Pass.
- `GROUPTICKET DELETE` of a future group ticket deletes, in order, the passes of each internal ticket, then the internal tickets, then the group ticket.
- A deletion that fails — by validation or because the data cannot be saved — leaves tickets and passes exactly as they were (in memory and on disk).
- `uml/model.puml` is reviewed and updated to represent the actual Delivery 3 design: passes, interfaces, generic attractions, associations and the other relevant relationships.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `ticket-management`: individual and group ticket deletion cascade to passes in a defined order and are atomic.
- `personal-passes`: pass persistence explicitly covers prices, ticket association and consumed status in both execution modes.
- `design-model`: the model must reflect the Delivery 3 design (passes, capability interfaces, generic attractions).

## Impact

- `IndividualTicket.clearPasses()`; `TicketController.delete/deleteGroupTicket` make the cascade explicit and rely on reload-on-failure for atomicity; tests for cascades, failed deletions, restart and `--fly` with Delivery 3 data.
- `uml/model.puml` reviewed against the code (PlantUML check + scripted cross-check + render).
