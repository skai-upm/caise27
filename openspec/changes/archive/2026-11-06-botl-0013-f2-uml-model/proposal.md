# Proposal

## Why

Milestone F2-C09 requires the Delivery 2 design to be documented as a PlantUML class diagram in `uml/model.puml`, and the diagram must match the submitted code. In particular it must show the ticket hierarchy, the composition between a group ticket and its individual tickets, and the relationships among users, groups, tickets and attractions. The repository has no UML model yet.

## What Changes

- Add `uml/model.puml`, a PlantUML class diagram of the current model and its collaborators. It shows classes, interfaces and records with their relevant attributes (no methods or constructors), inheritance and interface implementations, associations and compositions with directions and multiplicities.
- Represent the actual design. Users are not yet a class in the code: groups, tickets and attractions refer to users by email, and the diagram shows that faithfully with a note instead of inventing a `User` class.
- Add an automated consistency check that fails when a class or interface of the model, controller or persistence packages is missing from the diagram.

### In scope
- The PlantUML source file, its consistency check, and README reference.

### Out of scope
- Rendering images (only the `.puml` source is required). Model changes.

## Capabilities

### New Capabilities
- `design-documentation`: the PlantUML class model kept in the repository and its correspondence with the implemented design.

### Modified Capabilities
- None.

## Impact

- New `uml/model.puml`, and a new test `UmlModelConsistencyTest` that reads the file from the project directory. No production code changes.
