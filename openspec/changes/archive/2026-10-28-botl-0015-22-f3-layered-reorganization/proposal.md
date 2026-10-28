# Proposal

## Why

Milestone F3-C01 starts Delivery 3 by reorganising the code into distinct presentation, business and persistence packages while preserving all Delivery 2 behaviour, and requires exceptions to represent and propagate abnormal situations, handled in a controlled way at the appropriate level so that no incorrect command stops command processing.

## What Changes

- Move command processing and command classes to a `presentation` package; the domain model, park controller and business exceptions to `business` subpackages (`business.model`, `business.controller`, `business.exception`); repositories stay in `persistence`. The entry point `App` remains in the root package as the composition root that wires the layers.
- Introduce a business exception hierarchy rooted at `ParkException` (`NotFoundException`, `AlreadyExistsException`, `InvalidValueException`, `OperationNotAllowedException`) and `PersistenceException` for storage failures; business and persistence code throw them instead of generic `IllegalArgumentException`/`IllegalStateException`/`UncheckedIOException`.
- Presentation catches `ParkException` per command and reports it; a last-resort handler still reports unexpected runtime errors without stopping processing.
- Update the PlantUML model package names and tests; no externally visible behaviour change.
- Commit with the exact message `[F3-C01] Reorganize application into layered packages`.

## Capabilities

### New Capabilities
- `application-architecture`: Layered package organisation and exception-based handling of abnormal situations.

### Modified Capabilities
<!-- none: command behaviour is preserved -->

## Impact

- All source and test files move packages; imports updated; `uml/model.puml` package names updated.
- **Flagged ambiguities:**
  - Where the entry point belongs is not specified; `App` stays in the root (non-default) package as bootstrap code that configures storage and starts the presentation layer.
  - Exceptions are unchecked (extend `RuntimeException`) so they propagate through functional code; the source leaves the hierarchy design to the developer.
