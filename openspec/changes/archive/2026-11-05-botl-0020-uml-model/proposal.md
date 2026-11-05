# Proposal

## Why

Milestone F2-C09 requires the final Delivery 2 design to be documented as a PlantUML class diagram in `uml/model.puml` that corresponds to the submitted code: ticket hierarchy, group ticket composition, and the relationships among users, groups, tickets and attractions with their directions and multiplicities.

## What Changes

- Add `uml/model.puml` with the model classes, interfaces and enumerations, relevant attributes (no methods/constructors), inheritance, interface implementations, associations, compositions, directions and multiplicities.
- Add an automated consistency check that every model class appears in the diagram and the key relationships are declared.
- No behavior change.

## Capabilities

### New Capabilities
- `design-documentation`: The UML class diagram of the model and its consistency with the code.

### Modified Capabilities
<!-- None -->

## Impact

- New `uml/model.puml`; new test. No production code change.
