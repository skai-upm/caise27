# Proposal

## Why

Milestone F2-C09 requires the Phase 2 design to be documented as a PlantUML class diagram in `uml/model.puml`, consistent with the submitted code, correctly representing the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions.

## What Changes

- Add `uml/model.puml`: classes (and abstract classes), relevant attributes (no methods or constructors), inheritance, associations, compositions, directions and multiplicities, for the domain model, central management and the persistence and command layers.
- Add an automated consistency check that fails the build when a production class is missing from the diagram or the key relationships are not declared.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `project-build`: the repository contains the UML class model `uml/model.puml` consistent with the code.

## Impact

- New file `uml/model.puml`; new test `UmlModelTest`. No production code changes.

## Ambiguities (flagged)

- The diagram represents the code as it exists at this point of the batch (users have only email and DNI; no USER commands yet). Later proposals that change the model must update the diagram, and the consistency test enforces that every class is present.
- No PlantUML renderer is part of the build (external tools/libraries are not allowed in the runtime and none is required by the source); the check is textual. The file uses standard PlantUML class-diagram syntax.
- The project has no enumerations or interfaces yet; none are drawn.
