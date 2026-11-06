# Proposal

## Why

Milestone F3-C01 (Delivery 3, step 1) requires reorganizing the project into distinct Java packages for presentation, business and persistence responsibilities, preserving all Delivery 2 behavior, and using exceptions as the design mechanism for abnormal situations, handled in a controlled way at the appropriate level. Today the code is split by technical role (`command`, `controller`, `model`, `service`, `repository`, `persistence`) and the entry point sits in the root package.

## What Changes

- Move classes into layer packages under `es.upm.park`:
  - `presentation` — entry point, command interpretation and the input loop;
  - `business` (`business.model`, `business.service`, `business.controller`) — model, business rules, exceptions and central park control;
  - `persistence` (file stores, data directory) and `persistence.repository` — repositories and reading/writing persistent data.
- Update the JAR manifest main class to the new entry point; no class remains in the default or root package.
- Keep exceptions as the propagation mechanism: business operations throw `ParkException` subclasses (and persistence throws `PersistenceException`); the presentation layer catches them per command and turns them into messages, so a failing command never stops processing.
- Add an automated architecture check over the sources for package layout and dependency direction.

### In scope
- Source sections 3 and 4.

### Out of scope
- New behavior; Builder/Singleton/DI changes (F3-C02, F3-C03).

## Capabilities

### New Capabilities
- `layered-architecture`: layer packages, allowed dependencies between them, and controlled exception handling at the presentation boundary.

### Modified Capabilities
<!-- None: all behavioral specs are preserved unchanged. -->

## Impact

- Every source and test file moves package; `pom.xml` main class; `uml/model.puml` package names and `UmlModelTest` lookup packages; new `ArchitectureTest`.
