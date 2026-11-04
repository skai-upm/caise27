# Proposal

## Why

Delivery 3 state (passes, consumption, attraction types) must be reliably rebuilt after restarts. Deleting tickets currently leaves orphan passes behind. Valid ticket and group-ticket deletions must cascade to their passes atomically, and the UML must reflect the implemented Delivery 3 design.

## What Changes

- **Cascade deletion:**
  - A valid `TICKET DELETE` removes the ticket's SpeedPasses and Special Pass, then the ticket.
  - A valid `GROUPTICKET DELETE` removes the passes of every constituent, then the constituents and the group ticket (stored together).
- **Atomicity:** all validations run before any removal. Passes are removed and saved first; if the ticket or group-ticket save then fails, the removed passes are restored in memory and on disk, so no partial deletion survives.
- **Persistence audit:**
  - passes reload after tickets and group tickets, linked to the same individual ticket objects, preserving kind, price and consumed status;
  - attraction types persist, and legacy lines load as GENERAL;
  - `--fly` covers `passes.txt` and typed attractions.
  - These are confirmed by restart tests.
- **UML:** reviewed against the code and confirmed complete for Delivery 3.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `passes`: "Pass persistence" (rehydration order and preserved fields); added "Atomic cascade deletion".
- `individual-tickets`: "Ticket deletion" cascades to passes.
- `group-tickets`: "Group ticket deletion" cascades in the required order.
- `temporary-persistence-mode`: "All persistence redirected" includes passes and attraction types.
- `uml-model`: "PlantUML class model" covers the Delivery 3 design.

## Impact

- `ParkController` (delete orchestration), `TicketManager` (validation-only methods), `PassManager` (`removeFor`/`restore`), tests (failure-injecting storages, restart and `--fly` checks), `uml/model.puml` review.
- No command syntax or file format change.
