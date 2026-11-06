# Proposal

## Why

Milestone F3-C01 opens Delivery 3. The application must be reorganized into distinct presentation, business and persistence packages, and exceptions must represent and propagate abnormal situations as part of the design, handled at the appropriate level. All behavior achieved at the end of Delivery 2 must be preserved. Today the code is split by technical role (`command`, `controller`, `model`, `persistence`). Business failures use one generic exception, and persistence I/O failures surface as raw `UncheckedIOException`s, which the command loop reports only generically.

## What Changes

- Move the code into layered packages under `es.upm.park`:
  - `presentation`: command parsing and processing, the command loop and command-file reading.
  - `business`: `business.model` (domain objects), `business.controller` (central park management and the ports it needs) and `business.exception` (the exception hierarchy).
  - `persistence`: repositories and text storage, unchanged in behavior.
  - `es.upm.park.App` stays as the composition root that parses arguments and wires the layers. No class is in the default package.
- Introduce an exception hierarchy under an abstract `ParkException`: `NotFoundException` (unknown group, ticket, attraction or user), `BusinessRuleException` (operation not allowed by the rules), `InvalidInputException` (malformed values such as dates or capacities) and `StorageException` (persistent data could not be written). Operations throw them, and the presentation layer turns them into `ERROR:` messages while processing continues.
- Persistence adapters wrap I/O failures in `StorageException`. A failed save is reported, and the session continues.
- Enforce the dependency rules with an automated architecture check: presentation does not use persistence, business does not use presentation or persistence, and domain objects never read or write their persistent form.
- Update the PlantUML model and its consistency check to the new packages and exception types.

### In scope
- Package reorganization, exception design, architecture check, UML update.

### Out of scope
- Any new command or behavior change visible to users, beyond reporting a failed save as an error instead of a generic failure. User model and commands (later milestones in this batch).

## Capabilities

### New Capabilities
- `layered-architecture`: the layer structure of the application, its dependency rules, and exception-based handling of abnormal situations.

### Modified Capabilities
- `design-documentation`: the consistency requirement now covers the business and persistence layers instead of the old technical packages.

## Impact

- Every source and test file changes package (moved with `git mv`) and imports are updated. The JAR main class `es.upm.park.App` is unchanged.
- New exception classes. Throw sites switch to the specific types.
- New `LayeredArchitectureTest`. `uml/model.puml` and `UmlModelConsistencyTest` are updated.
