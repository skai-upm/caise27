# Design

## Context

Packages today: `command` (CommandProcessor, CommandRunner, CommandResult), `controller` (ParkController, ParkException, UserDirectory, ParkStorage, ParkState), `model` and `persistence`, plus the root `App`, which also reads command files. `ParkStorage` is already a port owned by the controller and implemented by `persistence.TextParkStorage`, so dependencies already point inward.

## Goals / Non-Goals

**Goals:** a clear layer per package with enforced dependency directions, and exceptions as design elements.
**Non-Goals:** renaming classes, changing behavior or output (except for failed saves).

## Decisions

- **Target packages**:
  - `es.upm.park.presentation`: CommandProcessor, CommandRunner (now also opens command files), CommandResult.
  - `es.upm.park.business.model`: Group, Ticket, IndividualTicket, GroupTicket, Attraction.
  - `es.upm.park.business.controller`: ParkController, UserDirectory, ParkStorage, ParkState.
  - `es.upm.park.business.exception`: ParkException and its subclasses.
  - `es.upm.park.persistence`: unchanged.
  - `es.upm.park.App`: composition root (arguments, data directory, wiring, `--fly` cleanup). Keeping it outside the layers is what lets presentation stay free of persistence imports.
- **Exception hierarchy** (unchecked, because callers in the business layer cannot recover and the presentation layer is the single handling point): abstract `ParkException`, then `NotFoundException`, `BusinessRuleException`, `InvalidInputException` and `StorageException`. The model's own invariant checks (`IllegalArgumentException` in `Attraction`) stay, because they guard programming errors that the controller validates before construction.
- **Handling levels**: the presentation `CommandProcessor` catches `ParkException` and prints `ERROR: <message>`. `CommandRunner` still catches any other `RuntimeException` as a last resort. Persistence converts `IOException` into `StorageException("Data could not be saved: ...")`. Load-time problems keep the existing skip-and-warn behavior.
- **Architecture check**: `LayeredArchitectureTest` scans `src/main/java` and fails if a `presentation` file imports `es.upm.park.persistence`, or a `business` file imports `es.upm.park.presentation` or `es.upm.park.persistence`. It also checks that every source declares a package.
- **Moves with `git mv`** keep file history. Imports are rewritten mechanically and checked by compiling and running all tests.

## Risks / Trade-offs

- [A large mechanical diff] → No logic changes besides the exception types. The full test suite, including the Delivery 2 integration test, guards behavior.
- [The state of a failed save] → The in-memory change remains, and the error tells the user the data was not stored. Rolling back in-memory state is out of scope.
