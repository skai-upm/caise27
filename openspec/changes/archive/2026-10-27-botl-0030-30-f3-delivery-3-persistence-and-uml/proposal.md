# Proposal

## Why

Milestone F3-C09: the Delivery 3 elements must survive restarts — attraction types (already stored since F3-C07) and, still missing, SpeedPasses and Special Passes with their ticket, stored price and consumed state — and `uml/model.puml` must describe the final Delivery 3 design.

## What Changes

- Persist passes in `passes.txt` (text, no serialization): type, identifier, associated individual ticket (direct or inside a group ticket), stored price and consumed flag; reload them linked to the loaded tickets with the same price and state.
- Save passes whenever they are bought, refunded, consumed or removed with their ticket; consumed passes stay stored while their ticket exists.
- `--fly` mode covers passes through the same repository with paths configured from the temporary directory.
- Attraction occupants remain non-persistent; attraction type persistence (F3-C07) is kept and covered by restart tests.
- Rewrite `uml/model.puml` for Delivery 3: access interfaces, generic attractions and their types, ticket–pass relationships with cardinalities, invoice Builder abstractions and director, singleton controllers, the initializer's dependency injection and the password hasher; extend the diagram–code consistency test to controllers and invoice classes.
- Commit with the exact message `[F3-C09] Extend delivery 3 persistence and UML`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `park-persistence`: passes with price and consumption state persisted; `--fly` covers them.
- `design-model`: diagram updated for Delivery 3 elements.

## Impact

- New `persistence.PassFileStore`; `DataPaths.passes()`; `PassRepository` optional store + `save()`; `Pass` restoring constructor; `ApplicationInitializer` load order (users → groups → tickets → passes → attractions); controllers save after consumption; `uml/model.puml`; `UmlModelTest`.
- Ambiguity flagged: a stored pass whose ticket no longer exists is skipped on load (tickets with passes are only deleted together with them).
