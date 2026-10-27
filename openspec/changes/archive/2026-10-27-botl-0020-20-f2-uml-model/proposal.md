# Proposal

## Why

Milestone F2-C09 requires the Phase 2 design to be documented as a PlantUML class diagram stored in the repository at `uml/model.puml`, consistent with the submitted code; evaluation checks its consistency with the implementation, not only its appearance.

## What Changes

- Add `uml/model.puml` with the classes, abstract class and enumeration of the model, relevant attributes (no methods or constructors), inheritance, associations, compositions, relationship directions and multiplicities.
- Represent in particular the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions, plus the central controller and repositories.
- Add an automated consistency check that every production class appears in the diagram.
- Record the mandatory milestone commit `[F2-C09] Add PlantUML class model` once verified.

## Capabilities

### New Capabilities
- `design-documentation`: Design artefacts kept in the repository (the PlantUML class model) and their consistency with the code.

### Modified Capabilities
<!-- None. -->

## Impact

- New `uml/model.puml`; new test `UmlModelTest`. No production code change.
