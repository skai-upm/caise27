# Proposal

## Why

`uml/model.puml` was grown incrementally over Delivery 2 and does not follow the course's diagram conventions (it mixes usage arrows freely, uses aggregation diamonds for plain references and does not mark navigability with a cross). The delivery asks for a faithful class model of the code as it is now, drawn with those conventions.

## What Changes

- Regenerate `uml/model.puml` from the current implementation only: classes, the abstract `Ticket` hierarchy, the `UserField` enumeration, associations/compositions with direction, multiplicities and names equal to the holding attribute.
- Apply the conventions: no methods, no usage relationships except to show the use of classes that have no incoming relationship, attributes only for primitive/value data (no relationship attributes, no technical labels such as visibility or `final`), and a cross on the owner end marking navigability.
- No production or test code changes.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `domain-model`: the model must reflect the current code and follow the diagram conventions.

## Impact

- Only `uml/model.puml` changes. Behavior is unchanged.
