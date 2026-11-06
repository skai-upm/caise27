# Proposal

## Why

Milestone F3-C09 requires persistence to keep the Delivery 3 elements and states: each attraction's type, and the SpeedPasses and Special Passes with their ticket, stored price and consumed state. Consumed passes stay stored while their ticket exists, and valid ticket deletions apply the pass cascades. It also requires the PlantUML model to represent the final Delivery 3 design. The storage of these elements was added incrementally by earlier Delivery 3 changes, but it was never verified end to end across restarts and `--fly` runs. The diagram does not yet show pass cardinalities per ticket.

## What Changes

- Verify through the packaged entry point that a restart reconstructs attraction types, passes with their tickets (including composed tickets), prices and consumed state. Also verify that cascading deletions are reflected in stored data, and that a `--fly` run handles the same elements and leaves nothing behind.
- Specify the stored-data effect of cascades: a valid ticket or group ticket deletion removes the affected passes from stored data, while consumed passes of existing tickets remain.
- Complete `uml/model.puml` for Delivery 3: SpeedPass (0..*) and Special Pass (0..1) cardinalities per individual ticket. Add a check that the diagram identifies the access interfaces, generic attractions and types, builder classes, injected relationships and singleton controllers.

### In scope
- Persistence verification and requirements, UML completion and its check.

### Out of scope
- User persistence (F1-C04, later in this batch). No new runtime dependencies.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `park-persistence`: adds the requirement on stored passes during cascades and their persistence in `--fly` mode.
- `design-documentation`: adds the Delivery 3 diagram content requirement.

## Impact

- New `Delivery3PersistenceTest`. UML diagram and `UmlModelConsistencyTest` are extended. No production behavior changes unless a verification fails.
