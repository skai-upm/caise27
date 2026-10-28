# Design

## Context

All Delivery 1 commands exist. `CommandProcessor.process` catches `UserOperationException` only; an unforeseen `RuntimeException` would propagate to `App.run` and end the application. See proposal.md.

## Goals / Non-Goals

**Goals:** containment of unexpected failures; reproducible end-to-end verification; documentation.
**Non-Goals:** new features from Delivery 2.

## Decisions

- **Catch `RuntimeException` in `CommandProcessor.process`** and return `ERROR: Unexpected error processing command`. Placed in the processor (not `App`) so both input modes share it.
- **Integration test** `DeliveryOneIntegrationTest` runs the statement session through `App.execute` from a file and from stdin on fresh data files and asserts outputs and final `users.txt`.
- **README** describes `mvn package`, both execution modes, the data file and command syntax.
- **`.gitignore`** gains `users.txt` so runtime data is not committed.
- **Audit** by inspection commands recorded in the tasks (dependencies, imports, regex APIs, git log).

## Risks / Trade-offs

- [A broad catch can hide bugs] → message is explicit and tests cover expected paths with specific errors.
