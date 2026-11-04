# Proposal

## Why

`uml/model.puml` has been extended incrementally and mixes notations (methods, usage arrows, technical labels, object-typed attributes duplicated by associations). The project needs a faithful, valid PlantUML class diagram of the code as it is now, following explicit notation rules, without changing the application.

## What Changes

- Regenerate `uml/model.puml` from the current Java sources (package `es.upm.park`), documentation only.
- Show classes, the abstract ticket class, records, the `UserField` enumeration, inheritance, associations, aggregations and compositions with navigability and multiplicities inferred from the fields.
- Notation rules from the source: no methods in classes; no technical labels on attributes; attributes only of primitive/value or enumeration types (object references to modelled classes appear only as relationships); relationship names equal the holding attribute name; each relationship line marks direction with a cross at the holder end and an arrow at the referenced class (or a diamond for aggregation/composition); no inverse relationships; usage (dependency) arrows only for classes that otherwise have no incoming relationship.
- No production or test code changes.

The source requests the change name `document-uml-model`; this batch uses the BOTL ID `botl-0009-9-f2-generate-uml-model-of-the-current-project-state`.

**Conflict resolved explicitly (see design.md):** the source lists "relevant public methods" but its acceptance criteria say "Do not represent usage relationships or methods in classes"; the acceptance criteria are applied (no methods, usage arrows only where a class has no incoming relationship, as the source also requires).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `uml-model`: the model requirement now states it is derived from the current implementation and its scenarios no longer require methods; new requirements define the notation rules.

## Impact

- `uml/model.puml` only. Behavior, tests and data formats unchanged.
