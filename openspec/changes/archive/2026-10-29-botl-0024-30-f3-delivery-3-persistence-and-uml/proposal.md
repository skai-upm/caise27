# Proposal

## Why

Milestone F3-C09 requires persistence to preserve and rebuild the Delivery 3 elements and states (attraction types, SpeedPasses and Special Passes with their individual tickets, stored prices and consumed state) in normal and `--fly` modes with the same repositories, and the PlantUML model to represent the final Delivery 3 design.

## What Changes

- Verify end to end, through the application entry point, that a Delivery 3 state (typed attractions, passes on individual and group-ticket components, consumed and unconsumed passes) survives a restart, that consumed passes stay stored while their ticket exists, that cascade deletions are persisted, and that `--fly` includes the new elements and leaves no data. Attraction occupants stay unpersisted.
- Complete `uml/model.puml`: SpeedPass and Special Pass cardinalities per individual ticket (`0..*` and `0..1`), and make the required Delivery 3 elements checkable (access interfaces, generic attractions and their types, Builder, injected relationships, invoice construction, singleton controllers).
- Extend the UML consistency test with the Delivery 3 checklist.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `persistence`: Delivery 3 elements and states persisted and rebuilt in both modes.
- `project-build`: the PlantUML model shows the final Delivery 3 design elements.

## Impact

- New integration test, `uml/model.puml`, `UmlModelTest`. No production code changes expected; any persistence defect found is fixed here.

## Ambiguities (flagged)

- The source lists the UML content; the textual check verifies the presence of those elements, not the visual rendering.
