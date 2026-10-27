# Proposal

## Why

Milestone F3-C01 starts Delivery 3 by reorganising the code into distinct presentation, business and persistence packages while preserving all Delivery 2 behaviour, and by making exceptions the way abnormal situations are represented and propagated to the level that handles them.

## What Changes

- Move every class from the single package `es.upm.amusementpark` into layer packages: `presentation` (entry point, command reading and processing), `business` (`model`, `service` incl. the central controller, `exception`) and `persistence` (repositories, file storages, data directory handling).
- The presentation layer no longer builds file storages or handles data directories: loading the park from a data directory and managing the `--fly` temporary directory move to business/persistence.
- Exceptions: a common business exception base (`ParkException`) for the existing operation exceptions, a `PersistenceException` for storage failures, and an `InvalidCommandException` for malformed commands; all are handled in one place in command processing so an incorrect command never stops processing.
- No change in commands, outputs or persisted formats.
- Record the mandatory milestone commit `[F3-C01] Reorganize application into layered packages` once verified.

## Capabilities

### New Capabilities
- `application-architecture`: Structural rules of the application — layered packages and their responsibilities, and how exceptional situations are represented and handled.

### Modified Capabilities
- `command-processing`: incorrect commands are reported through exceptions handled by the command processing layer (behaviour unchanged).

## Impact

- All production and test classes change package; `App`, `CommandProcessor`, `ParkController`, storages and exceptions are refactored. Build, JAR name and `Main-Class` updated to `es.upm.amusementpark.presentation.App`.
