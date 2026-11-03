# Proposal

## Why

`uml/model.puml` was grown incrementally in previous changes, mixing methods, usage arrows and object-typed attributes. The course now requires a faithful class diagram of the current implementation following specific notation rules, without touching the code.

## What Changes

- Regenerate `uml/model.puml` from the current Java sources: all classes, the abstract `ParkTicket`, the `UserField` enumeration, inheritance, associations, aggregations and compositions with direction, multiplicities and attribute-named labels.
- Apply the source notation: no methods, no usage relationships except for classes without incoming relationships, no technical labels on attributes, only primitive/enumeration/value attributes (object references only as relationship lines), relationship names equal to the holder's attribute name, and a cross on the holder end marking navigability.
- No production or test code changes; no invented elements.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `domain-model-diagram`: the diagram requirement now states it is derived from the implementation, and a notation requirement is added.

## Impact

- Only `uml/model.puml` and the `domain-model-diagram` spec. Application behaviour and tests are unchanged.
