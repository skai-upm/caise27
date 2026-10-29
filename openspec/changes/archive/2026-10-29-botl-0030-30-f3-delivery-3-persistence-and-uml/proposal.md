# Proposal

## Why

Milestone F3-C09: Delivery 3 introduced attraction types and passes whose purchases and consumption must survive restarts, and the PlantUML model must document the final Delivery 3 design.

## What Changes

- Persist SpeedPasses and Special Passes in `passes.txt`: identifier, kind, associated individual ticket, stored price and consumed state; reconstruct them on startup linked to their (standalone or composition) individual ticket. Attraction types are already persisted (F3-C07).
- Passes keep the price computed at purchase as a stored attribute (instead of recomputing it), so the persisted price is the purchase price.
- Save the park state also after `ATTRACTION ENTER` (pass consumption changes persistent state). Ticket deletions keep cascading to passes and the file reflects it. Occupants remain unpersisted.
- `--fly` covers `passes.txt` through the same storage classes configured by path.
- Update `uml/model.puml` to the final Delivery 3 design (access interfaces, generic attraction hierarchy and types, ticket–pass relationships with cardinalities, invoice Builder classes, dependency-injection composition root and dependencies, singleton controllers, help strategy).
- Commit with the mandatory message `[F3-C09] Extend delivery 3 persistence and UML`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `park-persistence`: passes with price and consumed state are persisted and reconstructed; consumption is saved.
- `design-documentation`: the class model shows the Delivery 3 design elements.

## Impact

- `Pass`/`SpeedPass`/`SpecialPass` (stored price), new `PassFileStorage`, `DataLocation.passes()`, `ParkStorage`, `ParkController` load/save, `CommandProcessor` save trigger, `.gitignore`, UML and tests.
- Ambiguity flagged: a pass whose ticket cannot be resolved on load is skipped with a warning (it cannot exist without its ticket).
