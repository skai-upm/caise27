# Proposal

## Why

Delivery 3 state (passes with price and consumption, attraction types) must survive restarts, deleting tickets must not leave orphan passes, and the PlantUML model must describe the final E3 design. Passes and attraction types are already persisted (C06, C07); until now tickets with passes simply could not be deleted.

## What Changes

- **Cascade deletion**: deleting a future individual ticket (`TICKET DELETE`) also deletes its SpeedPasses and Special Pass; deleting a future group ticket (`GROUPTICKET DELETE`) deletes the passes of each composing ticket, then those tickets, then the group ticket. This replaces the C06 rule that rejected deleting tickets with passes.
- Every deletion condition is checked before anything is removed, so a rejected deletion leaves no partial deletions.
- Verified persistence of passes (ticket, type, price, consumed status) and attraction types across restarts, in normal and `--fly` modes.
- `uml/model.puml` re-derived from the code for the E3 design (classes, interfaces, generics, associations) and cross-checked against the source fields.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `passes`: tickets with passes are deleted in cascade instead of being protected.
- `fly-mode`: the temporary directory also holds passes.

## Impact

- `TicketController` (deletion checks separated from deletion), `PassController` (`deletePassesOf`), `CommandProcessor` (cascade orchestration), tests, `uml/model.puml`.
