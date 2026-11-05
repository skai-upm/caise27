# Proposal

## Why

Milestone F3-C01 (Delivery 3, step 1) requires the application to be reorganized into distinct packages for presentation, business and persistence, with no default package, while preserving the Delivery 2 behavior (source §3). Abnormal situations must be represented and propagated with exceptions and handled in a controlled way at the appropriate level, so that an incorrect command never stops command processing (§4). Today, business rules (validation, uniqueness, ticket and group rules, capacity) live in the command classes and are reported by building error strings. Persistence repositories and the business controller share a package-per-concept layout, and the entry point creates file stores itself.

## What Changes

- Reorganize the code into three layers under `es.upm.parque`:
  - `presentation`: the entry point, command interpretation and response formatting.
  - `business`: the central park management, the operations implementing business rules, system values, and `business.model` with the domain classes.
  - `persistence`: the repositories, the text-file stores and the storage location (normal or `--fly`).
- Move business rules out of the command classes into business operations that throw exceptions from a business exception hierarchy:
  - a base park exception;
  - not-found;
  - invalid-data;
  - rule-violation.
  Presentation parses commands, reports malformed syntax with its own exception, and catches both at one place, turning them into the same `ERROR: <message>` responses as before. Unexpected runtime failures are still caught there, and processing continues.
- The presentation layer stops accessing persistence directly: the entry point opens and closes the park's storage through a business-level operation. Domain objects keep doing no persistence.
- Behavior is preserved: commands, responses, persistence formats and `--fly` are unchanged. The existing test suite keeps passing after moving to the new packages.
- Update the UML model to the new packages and the classes introduced.

In scope: package reorganization, the exception-based error flow, and the presentation-persistence decoupling.

Out of scope: new commands or behavior (later Delivery 3 milestones).

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering (presentation, business, persistence, no default package), dependency rules between layers, and exception-based handling of abnormal situations.

### Modified Capabilities
- None. All behavioral requirements in existing capabilities remain unchanged.

## Impact

- Every source file moves package. `pom.xml` `mainClass` becomes `es.upm.parque.presentation.App`. New business managers and exception classes. `uml/model.puml` and the UML consistency test follow the packages. Tests move with their classes.
