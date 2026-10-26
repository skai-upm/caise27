# Proposal

## Why

`uml/model.puml` has been extended incrementally and mixes conventions (usage arrows everywhere, technical labels on attributes). Delivery 2 requires a PlantUML class diagram that faithfully documents the current object-oriented design of the Amusement Park project, derived from the actual code and following explicit diagram conventions.

## What Changes

- Regenerate `uml/model.puml` from the current implementation: all relevant classes, the abstract ticket hierarchy, the `UserField` enumeration, associations, aggregations, compositions, navigability and multiplicities.
- Apply the diagram conventions: no methods and no general usage relationships; attributes only for primitive/value types and enumerations (relationships are not repeated as attributes); no technical labels on attributes; relationship names equal to the owning attribute's name; navigable lines with a cross on the owner side; usage shown only for classes that have no incoming relationship.
- No functional code change, refactoring or new behaviour.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `uml-model`: adds faithfulness and diagram-convention requirements to the PlantUML model.

## Impact

- `uml/model.puml` only. Source code and tests are unchanged.
