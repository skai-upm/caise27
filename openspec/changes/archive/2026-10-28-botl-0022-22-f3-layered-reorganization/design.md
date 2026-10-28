# Design

## Context

Packages today: root (`App`), `cli`, `model`, `service`, `park`, `repository`, `persistence`. All business failures use `service.OperationException`. `App` creates and deletes the `--fly` directory itself and builds `DataLocation`. See proposal.md.

## Goals / Non-Goals

**Goals:** clean layering with unchanged behavior; meaningful exception types.
**Non-Goals:** Delivery 3 features (builder, DI, passes...).

## Decisions

- **Package mapping**: `App`, `cli.CommandProcessor` → `presentation`; `park.ParkController` → `business`; `model.*` → `business.model`; `service.*` → `business.service`; `service.OperationException` → `business.exception.ParkException` (abstract base) + subclasses; `repository.*` → `persistence.repository`; `persistence.*` → `persistence.file`.
- **`--fly` lifecycle**: `persistence.file.DataLocation.temporary(Path root)` creates `root/<yyyyMMdd_HHmmss>[_n]`; `DataLocation.delete()` removes it recursively. `ParkController.initialize(Path dataDir)` / `initializeTemporary(Path root)` / `close()` are what `App` calls — presentation never touches files except reading the command file it was given (user input, not persistent data).
- **Exceptions**: checked `ParkException` (abstract) with `ValidationException`, `NotFoundException`, `ConflictException`, `OperationNotAllowedException`, `DataAccessException` (wraps `IOException`), and `presentation.CommandSyntaxException`. Each throw site picks the type matching the cause; messages are unchanged so output stays identical. `CommandProcessor` catches `ParkException` (and, as a last resort, `RuntimeException`) per command.
- **Save failures** from `ParkController.saveState()` surface as `DataAccessException`.

## Risks / Trade-offs

- [Large mechanical move] → done with scripted renames plus full test suite (behavior tests are unchanged).
