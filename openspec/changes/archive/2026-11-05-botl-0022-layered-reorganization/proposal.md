# Proposal

## Why

Milestone F3-C01 (Delivery 3) requires the code to be organised in distinct presentation, business and persistence packages while preserving all Delivery 2 behavior, and requires exceptions to represent and propagate abnormal situations, handled at the appropriate level so that incorrect commands never stop command processing. The current packages (`command`, `model`, `park`, `service`, `repository`, `persistence` and classes in the root package) do not express these layers, and rejections use a single generic exception type.

## What Changes

- Reorganise packages under `es.upm.amusementpark`:
  - `presentation`: `App`, `InteractiveSession`, `CommandProcessor` (user interaction and command processing);
  - `business`: `business.model` (domain), `business.control` (`Park`), `business.service` (rules), `business.exception` (exception hierarchy);
  - `persistence`: `persistence.repository` (repositories), `persistence.storage` (text-file storages, data directory handling).
- Exception hierarchy: `OperationException` (base of rejected operations) with `NotFoundException`, `InvalidDataException` and `RuleViolationException`; `PersistenceException` for storage failures. Services throw them, the presentation layer translates them into `ERROR: ...` responses; start-up storage failures end the run in a controlled way.
- The executable JAR's entry point moves to `es.upm.amusementpark.presentation.App`. No command behavior changes.

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering and dependency rules, and the exception-based handling of abnormal situations.

### Modified Capabilities
- `command-interface`: failures are reported according to their exception category (rejected operation vs. data storage problem).

## Impact

- Every source file moves package (history kept with `git mv`); `pom.xml` main class; tests mirror the new packages; UML package names.
