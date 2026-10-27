# Design

## Context

Packages today: `model`, `park`, `repository`, `persistence`, `command` and a root `Main`. Command classes mix syntax handling with business rules and signal failures by returning `"ERROR: …"` strings.

## Goals / Non-Goals

**Goals:** three layers; rules in business; exceptions as the failure channel; identical observable behaviour.
**Non-Goals:** Singleton per controller and dependency injection (F3-C03), new commands.

## Decisions

- **Package mapping**: `model`→`business.model`, `park`→`business.controller`, `repository`→`persistence.repository`, `command`+`Main`→`presentation`.
- **Business controllers** (`UserController`, `GroupController`, `TicketController`, `AttractionController`) receive the `ParkController` (state + repositories) and implement the rules formerly in command classes, throwing:
  - `ParkException` (checked base) → `NotFoundException`, `InvalidDataException`, `DuplicateException`, `OperationNotAllowedException`. Messages keep the former texts so output is unchanged.
- **Presentation**: `CommandProcessor` splits and dispatches; `CommandInput` validates field count and converts dates, amounts and numbers, throwing `InvalidCommandException`; handler classes call controllers and format results. A single `try/catch` maps `InvalidCommandException`/`ParkException` → `ERROR: <message>`, `PersistenceException` (unchecked, persistence layer) → `ERROR: Could not save data`, any other runtime failure → `ERROR: Command could not be processed`.
- **`--fly`**: `ParkController.startTemporary(base)` / `startPermanent(base)` / `close()` encapsulate `DataPaths` and `TemporaryDataDirectory`, so `Main` handles no files.
- `ParkController.setCurrentPrice` validates positivity itself (`InvalidDataException`), the amount syntax stays in presentation.

## Risks / Trade-offs

- [Large move touching every file] → done with `git mv` to keep history; full regression suite run after the move.
- [Check order changes when several fields are wrong at once, e.g. bad date and unknown user] → only which error is reported first can differ; single-error cases are unchanged.
