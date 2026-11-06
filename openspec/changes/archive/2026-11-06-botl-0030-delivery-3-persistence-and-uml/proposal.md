# Proposal

## Why

Milestone F3-C09 (Delivery 3, step 9) requires the new Delivery 3 state — attraction types, SpeedPasses and Special Passes with their ticket, stored price and consumed state — to survive restarts (also in `--fly` mode through the same repositories), and the PlantUML model to represent the final Delivery 3 design. Passes currently live only in memory and the diagram stops at the attraction hierarchy.

## What Changes

- Persist passes in `passes.txt`: type, identifier, owning individual ticket (direct or group-ticket constituent), stored price and consumed flag; reconstruct them at start-up linked to their ticket objects. Consumed passes stay stored while their ticket exists; ticket refunds keep removing their passes (and stored passes whose ticket no longer exists are dropped on load).
- Pass consumption at attraction entry is persisted.
- `--fly` covers passes via the same repository configuration; occupants stay unpersisted; no serialization or new libraries.
- Update `uml/model.puml` with passes and their cardinality per ticket, invoice Builder elements, dependency-injection relationships (repositories, password hasher, help formatter), singleton controllers; extend the UML consistency test accordingly.

### In scope
- Source sections 13 and 14.

### Out of scope
- Delivery 3 integration (F3-C10).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `park-persistence`: passes and their state are persisted and reconstructed.
- `uml-model`: the diagram represents the Delivery 3 design elements.

## Impact

- New `PassFileStore`; `PassRepository` backed by it; `Pass` reconstruction constructors; `TicketRepository.findIndividual`; `ParkRepositories.load`; `ParkController` persists consumption; `.gitignore`; `uml/model.puml`; `UmlModelTest`.
