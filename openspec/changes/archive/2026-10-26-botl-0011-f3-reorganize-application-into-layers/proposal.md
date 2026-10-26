# Proposal

## Why

All 22 classes live in a single package, mixing command handling, domain rules and file access. Delivery 3 starts by physically separating presentation, business and persistence so later design patterns (Builder, Singleton, dependency injection) have clear places to live.

## What Changes

- Move every class into layer packages under `es.upm.parque`:
  - `presentation` – command interpretation and the command loop.
  - `business.model` – domain objects; `business.controller` – domain rules per area; `business.validation` – data validation; `business` – the business exception.
  - `persistence` – repositories and the temporary data directory.
  - `es.upm.parque` keeps only the `App` entry point that assembles the layers.
- Rename the per-area `*Manager` classes to `*Controller` and `UserException` to `BusinessException`, matching their layer roles (it is raised by every controller, not only for users).
- Tests move to the matching packages; the PlantUML model is updated with the new names and packages.
- No command syntax or behavior changes; `mvn package` keeps producing the executable JAR.

## Capabilities

### New Capabilities
- `layered-architecture`: the package structure and the dependency rules between layers.

### Modified Capabilities
<!-- None: no externally visible behavior changes. -->

## Impact

- Every source and test file changes package/imports; class renames listed above. Main class stays `es.upm.parque.App`.
