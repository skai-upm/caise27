# Proposal

## Why

Milestone F3-C01 opens Delivery 3: the code must be organised into presentation, business and persistence packages, and abnormal situations must be represented and propagated with exceptions handled at the appropriate level — while preserving every Delivery 2 behaviour.

## What Changes

- Reorganise packages under `es.upm.amusementpark`:
  - `presentation` — `Main`, command processing and per-area command parsers (syntax, conversion, output formatting).
  - `business` — `business.model` (domain), `business.controller` (`ParkController` plus controllers holding the business rules for users, groups, tickets and attractions), `business.exception`.
  - `persistence` — file stores, data paths, temporary directory and `persistence.repository`.
- Business rules move out of command parsing into business controllers that throw checked exceptions (`ParkException` hierarchy: not found, invalid data, duplicate, operation not allowed); presentation reports syntax problems with `InvalidCommandException` and turns any exception into an `ERROR: …` line, so no command terminates processing.
- Presentation no longer touches persistence files: temporary (`--fly`) directory handling moves behind the park controller.
- Observable behaviour (commands, outputs, files, run modes) stays as at the end of Delivery 2, except that a write failure is now reported as `ERROR: Could not save data` (it now covers every data file, not only users).
- Commit with the exact message `[F3-C01] Reorganize application into layered packages`.

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering and exception-based handling of abnormal situations.

### Modified Capabilities
<!-- none: functional requirements are unchanged -->

## Impact

- All classes move package (main class becomes `es.upm.amusementpark.presentation.Main`, updated in `pom.xml`); tests move accordingly.
- New `business.controller.{User,Group,Ticket,Attraction}Controller`, `business.exception.*`, `presentation.{CommandInput,InvalidCommandException,UserCommands,SystemCommands}`.
- The PlantUML model is fully updated in F3-C09; until then only its package names are adjusted.
