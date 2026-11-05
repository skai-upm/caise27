# Proposal

## Why

Milestone F3-C09 requires persistence to keep every Delivery 3 element and state, and the PlantUML model to represent the final Delivery 3 design. Required elements are attraction types, SpeedPasses and Special Passes with their tickets, **their stored price** and consumed state, and cascaded deletions. Most of this is already persisted. Pass prices, however, are currently recomputed on load rather than stored, and the UML doesn't yet make the pass cardinalities explicit or guard the Delivery 3 elements.

## What Changes

- Passes keep a price fixed at purchase. It is computed once from the ticket price (20% for SpeedPass, 100% for Special Pass) and stored in the pass.
- The pass file stores it as `SPEED|SPECIAL;id;ticketId;price;consumed`. Older 4-field lines (without a price) still load, deriving the price as before.
- Persistence keeps consumed passes while their ticket exists. Cascaded deletions and refunds disappear from the persisted state. `--fly` covers all of it through the same repositories (**MODIFIED** `park-persistence`).
- The UML model is updated to the final Delivery 3 design: explicit cardinalities `IndividualTicket "1" -- "0..*" SpeedPass` and `IndividualTicket "1" -- "0..1" SpecialPass`, plus existing access interfaces, generic attractions, attraction types, Builder, dependency injection, singleton and `User`. The guard test checks these elements (**MODIFIED** `design-documentation`).

### Out of scope
- User persistence (user persistence milestone, later in this batch). Its repository will join the same `ParkStorage` configuration.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `park-persistence`: stored pass prices, consumed passes kept, cascades persisted, `--fly` coverage.
- `design-documentation`: Delivery 3 elements required in the model.

## Impact

- `Pass` and its subclasses (stored price).
- `ParkController` pass restore.
- `PassRepository` format.
- `uml/model.puml`, `UmlModelTest` and persistence tests.
