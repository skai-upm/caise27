# Design

## Context

Current packages: `command`, `model`, `park`, `service`, `repository`, `persistence`, root (`App`, `InteractiveSession`, `TemporaryDirectory`). Rejections use `OperationException`/`UserOperationException`; storage failures surface as `UncheckedIOException`.

## Goals / Non-Goals

**Goals:** clear layers with one-way dependencies `presentation → business ← persistence` (persistence implements storage of business objects; repositories are used by business services); categorised exceptions. **Non-Goals:** behavior changes, dependency injection (F3-C03).

## Decisions

- **Mapping**: `model` → `business.model`; `park` → `business.control`; `service` (services) → `business.service`; exceptions → `business.exception`; `repository` → `persistence.repository`; `persistence` → `persistence.storage`; `TemporaryDirectory` → `persistence.storage.DataDirectory` (creates/deletes the data directory; presentation only asks for it); `App`, `InteractiveSession`, `CommandProcessor` → `presentation`. Moves done with `git mv` to keep history.
- **Exception hierarchy** (`business.exception`): `OperationException` (abstract base, unchecked) → `NotFoundException`, `InvalidDataException`, `RuleViolationException`. `UserOperationException` is removed (replaced by the categories). `persistence.storage.PersistenceException` wraps `IOException`s. Unchecked exceptions keep lambdas in the command registry simple; the categories carry meaning for callers and tests.
- **Handling level**: services throw; `CommandProcessor` (presentation) maps `OperationException` → `ERROR: <message>`, `PersistenceException` → `ERROR: data could not be saved (<detail>)`, other `RuntimeException` → generic `ERROR`; `App` maps a start-up `PersistenceException` to a controlled exit (status 1).
- **Architecture test** verifying package placement and the dependency rules by scanning imports.

## Risks / Trade-offs

- [Large mechanical diff] → behavior protected by the existing 120 tests, moved with the code.
