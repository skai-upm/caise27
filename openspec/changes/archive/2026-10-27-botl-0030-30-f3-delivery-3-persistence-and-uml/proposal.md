# Proposal

## Why

Milestone F3-C09 makes the Delivery 3 state survive between executions — attraction types, SpeedPasses and Special Passes with their tickets, stored prices and consumed states — with the same repositories in normal and `--fly` modes, and brings the PlantUML model up to the final Delivery 3 design.

## What Changes

- Persist passes (type, identifier, individual ticket, stored price, consumed state) and reconstruct them linked to their tickets; consumed passes stay stored while their ticket exists; cascading ticket deletions are reflected in the stored passes.
- Attraction types remain persisted (added in F3-C07); attraction occupants remain unpersisted.
- `--fly` stores passes in its temporary directory through the same repositories.
- Update `uml/model.puml` to the final Delivery 3 design (access interfaces, generic attractions and types, ticket–pass relationships and cardinalities, Builder classes for user/group invoices, dependency-injection relationships, singleton controllers) and extend the automated consistency check.
- Record the mandatory milestone commit `[F3-C09] Extend delivery 3 persistence and UML` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `persistence`: passes and their states persisted and restored.
- `design-documentation`: the model shows the Delivery 3 design elements.

## Impact

- New `PassFileStorage`; `PassRepository` with storage; `ParkRepositories`, `ParkController.persistAll`; pass restore constructors; `uml/model.puml`; `UmlModelTest`.
