# Proposal

## Why

Delivery 3 state must survive restarts and stay consistent: passes with their prices and consumption, attraction types, and — when tickets are refunded — the passes that depend on them. Today a refunded ticket's passes only disappear from memory and the pass file keeps them until another pass operation, and nothing guarantees all-or-nothing deletion if storage fails. The design model must also reflect the final E3 design.

## What Changes

- `TICKET DELETE` cascade-deletes the ticket's SpeedPasses and Special Pass; the pass file is rewritten together with the tickets.
- `GROUPTICKET DELETE` deletes, in order, the passes of every individual ticket of its composition, then those individual tickets, then the group ticket.
- A deletion that fails (validation or storage) leaves nothing partially deleted: all checks run before any mutation and a storage failure restores the previous in-memory state and files.
- Confirmed persistence of passes (price, ticket, consumed status) and attraction types between executions, also under `--fly`.
- `uml/model.puml` updated with the new association and kept faithful to the E3 code (checked by `UmlModelTest`).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `ticketing`: "Refund individual ticket" cascades to passes and is all-or-nothing.
- `group-ticketing`: "Refund group ticket" defines the deletion order and is all-or-nothing.
- `personal-passes`: new requirement on cascade deletion with their tickets.
- `temporary-execution`: new requirement stating that all E3 data (passes, attraction types) uses the temporary directory.
- `design-model`: new requirement that the model represents the E3 design (generics, interfaces, pass composition).

## Impact

- `TicketManager` (pass repository injected, ordered cascade, rollback), `IndividualTicket` (clear/restore passes), `Bootstrap`, tests, UML.
