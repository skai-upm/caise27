# Proposal

## Why

Delivery 3 state must survive restarts and deletions must not leave orphan passes: deleting a future ticket or group ticket currently keeps their passes. The UML model must describe the final Delivery 3 implementation.

## What Changes

- Persistence already reconstructs passes (identity, price, ticket link, consumed status) and attraction types (with GENERAL for legacy lines); this change completes it with explicit verification of reconstruction order and consumed status.
- `TICKET DELETE` cascades to the ticket's SpeedPasses and Special Pass; `GROUPTICKET DELETE` deletes the passes of every constituent ticket, then the constituents, then the group ticket.
- Deletions are validated before any change and are atomic: if storing fails, removed passes are restored, so no partial deletion remains.
- `--fly` isolation explicitly covers passes and typed attractions.
- `uml/model.puml` checked field by field against the implementation.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `individual-tickets`: ticket deletion cascades to passes atomically.
- `group-tickets`: group ticket deletion cascades through the composition atomically.
- `pass-file-persistence`: restart keeps consumed status (scenario added).
- `temporary-persistence-mode`: the isolated data includes passes and attraction types.
- `domain-model-diagram`: the diagram must match the Delivery 3 implementation field by field.

## Impact

- `ParkController` (cascade orchestration), `PassController` (remove/restore passes of tickets), `TicketController` (deletion pre-checks), tests, `uml/model.puml`.
