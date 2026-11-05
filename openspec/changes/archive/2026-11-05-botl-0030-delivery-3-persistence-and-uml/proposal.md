# Proposal

## Why

Milestone F3-C09: the Delivery 3 elements must survive restarts — attraction types, SpeedPasses and Special Passes with their tickets, stored prices and consumed state — in normal and `--fly` mode, and `uml/model.puml` must represent the final Delivery 3 design. Attraction types are already stored since F3-C07; passes currently live only in memory, and the diagram lacks controllers, builders and injection relationships.

## What Changes

- Persist passes in `passes.txt` next to the other data files (type, identifier, individual ticket, stored price, consumed flag), loaded after tickets; consumed passes remain stored while their ticket exists; ticket deletions cascade to the file.
- `--fly` covers passes automatically (same repository factory, different directory). Occupants stay non-persistent. No serialization or new libraries.
- Update `uml/model.puml` with the access interfaces, generic attractions and their types, ticket–pass relationships with cardinalities (0..* SpeedPasses, 0..1 Special Pass per ticket), the invoice Builder abstractions, the singleton controllers and the dependency-injection relationships of the composition root; extend the consistency test.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `park-persistence`: adds persistence of passes and attraction types.
- `design-documentation`: adds the Delivery 3 diagram content requirements.

## Impact

- New `PassStorage`/`PassFileStorage`; `PassRepository` saves through storage; `Repositories.files`; `uml/model.puml`; tests.
