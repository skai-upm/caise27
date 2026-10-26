# Proposal

## Why

Delivery 3 added passes, consumption and attraction types. Their state must survive restarts, ticket deletions must cascade to passes in a defined order without ever leaving partial deletions, `--fly` must isolate the new data too, and the PlantUML model must describe the final E3 design.

## What Changes

- Verify and specify reconstruction of passes (ticket, price, status incl. CONSUMED) and of attraction types between executions (already stored in `passes.txt` / `attractions.txt`).
- `TICKET DELETE` of a valid future individual ticket explicitly deletes its SpeedPasses and Special Pass, then the ticket.
- `GROUPTICKET DELETE` of a valid future group ticket deletes the passes of each internal ticket first, then the internal tickets, then the group ticket.
- Deletion is all-or-nothing: rules are checked before any change, and if saving fails the in-memory state is restored, so a failed deletion leaves nothing partially deleted.
- `--fly` covers the pass file (all files live in the temporary directory).
- Review `uml/model.puml` against the E3 code (classes, interfaces, generics, associations) and complete it.
- Required commit title: `[F3-C09] Extend delivery 3 persistence and UML`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `personal-passes`: pass persistence includes consumed status/price reconstruction and ordered cascade deletion.
- `ticket-management`: atomic cascading deletion of individual and group tickets.
- `temporary-persistence`: the temporary directory holds every persistence file, including passes.

## Impact

- `IndividualTicket` / `GroupTicket` gain explicit cascade/restore operations; `TicketManager` deletion becomes transactional; tests for restart, cascade, failure rollback and `--fly`; UML reviewed.
