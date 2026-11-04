# Proposal

## Why

The project has a hand-maintained diagram at `docs/uml/model.puml`, but the delivery requires the model at `uml/model.puml`, derived strictly from the current code and drawn with specific notation rules (no methods, relationship names equal to attribute names, crossed non-navigable ends, only datatype attributes). This change documents the actual implementation; it does not alter it.

## What Changes

- Create `uml/model.puml`, a valid PlantUML class diagram of the current code:
  - every production class, the abstract `Ticket`, and the `UserField` enum
  - inheritance (`Ticket` ← `IndividualTicket`, `GroupTicket`; `Exception` ← `UserException`)
  - associations, aggregations and compositions, with direction and multiplicities taken from the fields that exist
- Notation rules applied:
  - no methods in classes
  - attributes without visibility or technical labels, limited to primitive / datatype values and enums
  - object references drawn only as relationships, named after the holding field
  - the non-navigable end marked with a cross
  - no usage relationships, except to show how classes without any incoming relationship are used
- Remove `docs/uml/model.puml`, so that a single model lives at the required location.
- Add a test that fails if a production class is missing from `uml/model.puml`, which keeps the model aligned with the code.
- No production code changes.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `domain-model-diagram`: the model moves to `uml/model.puml` and must follow the delivery's notation rules. Scenarios are updated to the actual attribute names (for example `user`, `present`, `parts`).

## Impact

- New `uml/model.puml` and `src/test/java/.../UmlModelTest.java`; deleted `docs/uml/model.puml`.
- Production sources, commands and data files unchanged.
