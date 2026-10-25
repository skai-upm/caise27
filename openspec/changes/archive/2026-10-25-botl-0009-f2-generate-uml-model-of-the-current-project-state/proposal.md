# Proposal

## Why

The PlantUML file has been updated incrementally and partly follows informal conventions (visibility marks, `{readOnly}` labels, a stereotype, generic usage arrows). The object-oriented design must be documented faithfully from the actual implementation, following precise notation rules, so the diagram can be relied on as the reference model of the project's current state.

## What Changes

- Regenerate `uml/model.puml` from the current code only: every class, the abstract class `Ticket`, its subclasses, and the `UserField` enumeration.
- Attributes: only primitive/value-typed fields and enumerations, without technical labels (no visibility marks, `{readOnly}`, stereotypes); fields that hold other classes are drawn only as relationships.
- Relationships: inheritance, associations, aggregations and compositions with direction (navigability as in the code, no invented inverse relationships), multiplicities on both ends, the name of the holding attribute as relationship name, and the non-navigable cross on the holder side.
- No methods in classes and no usage (dependency) lines, except for classes without any incoming relationship, whose usage is shown.
- No functional code changes. A test checks the model against the classes by reflection so the diagram stays faithful.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `design-model`: the PlantUML requirement now states the notation rules (attributes, relationship naming, navigability marks, multiplicities, no methods, usage only for classes without incoming relationships) and that the model is derived from the current code without inventing elements.

## Impact

- `uml/model.puml` rewritten; new test `UmlModelTest` (test code only). No production code or behavior changes.
