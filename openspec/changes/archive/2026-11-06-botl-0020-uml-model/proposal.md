# Proposal

## Why

Milestone F2-C09 (Delivery 2, step 9) requires the final Phase 2 design to be documented as a PlantUML class diagram at `uml/model.puml`, consistent with the submitted code: ticket hierarchy, group-ticket composition and the relationships among users, groups, tickets and attractions, with directions and multiplicities.

## What Changes

- Add `uml/model.puml`: classes, interfaces, relevant enumerations, inheritance, associations, compositions, directions and multiplicities of the model (and the central park management that owns it); relevant attributes only, no methods or constructors.
- Add an automated consistency check (unit test) that compares the diagram's declared classes, enumerations, inheritance and key relationships with the actual code, so the diagram cannot silently drift.

### In scope
- Source section 14.

### Out of scope
- Code changes to the model; Delivery 2 integration (F2-C10).

## Capabilities

### New Capabilities
- `uml-model`: the PlantUML class model of the park design and its consistency with the implementation.

### Modified Capabilities
<!-- None. -->

## Impact

- New `uml/model.puml`; new test `UmlModelTest`. No production code changes.
