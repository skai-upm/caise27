# Proposal

## Why

Milestone F3-C09 requires persistence to preserve and reconstruct the Delivery 3 elements and states (attraction types; SpeedPasses and Special Passes with their individual ticket, stored price and consumed state; consumed passes kept while their ticket exists; cascading deletions) in normal and `--fly` mode with the same repositories, and the PlantUML model `uml/model.puml` to represent the final Delivery 3 design.

## What Changes

- Verify end to end that the Delivery 3 state (attraction types, passes with ticket/price/consumed state, cascading deletions) survives restarts and that `--fly` covers it through the same repositories without leaving data; attraction occupants stay non-persistent.
- Update `uml/model.puml` to the final Delivery 3 design: ticket access interfaces, generic attractions and their types, passes and their cardinalities per individual ticket (any number of SpeedPasses, at most one Special Pass), the invoice Builder abstractions (builder, concrete builders, director, product, sections/items), the singleton controllers and the dependency-injection relationships (repository ports and their text-file implementations, password hasher, controller collaboration, composition root).
- Extend the automated UML consistency check to every business package (model, controller, invoice, repository, security) and to the persistence implementations.
- Commit with the exact message `[F3-C09] Extend delivery 3 persistence and UML`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `park-persistence`: Delivery 3 state persisted in both modes.
- `design-documentation`: diagram content and consistency extended to the Delivery 3 design.

## Impact

- `uml/model.puml`, `UmlModelTest`, new Delivery 3 persistence integration test; no production behaviour change expected (persistence of the new elements was introduced by F3-C06/F3-C07 and is verified here).
- **Flagged ambiguity:** exceptions, presentation commands and helper classes are not drawn ("not necessary to represent every implementation detail or every defined exception"); the composition root `App` and `CommandProcessor` are drawn only for the injection relationships.
