# Design

## Context

Commands are dispatched by a `switch` in `CommandProcessor.process`; help text comes from `CommandRegistry.standard()` (29 entries, several commands documented with variants). `App.launch(args, in, out, dataDir, tmpDir, start)` selects interactive/file mode and `--fly`. A manual comparison shows every dispatched operation is registered and vice versa, but nothing enforces it.

## Goals / Non-Goals

**Goals:**
- Automated evidence of integration: registry ↔ dispatcher, one combined script across modes, restart, packaged JAR.

**Non-Goals:**
- New commands or behavior changes.

## Decisions

- **Registry/dispatcher check** in `CommandProcessorTest`: for every `CommandInfo` syntax, take the operation (text before the first `;`, or the whole syntax) and process it with the documented number of placeholder parameters; assert the response is not `ERROR UNKNOWN COMMAND`. Conversely, an unregistered operation answers `ERROR UNKNOWN COMMAND`. Both help modes are checked for the E3 commands.
- **Combined script** in `AppTest`: one list of commands (E1 users/system, E2 groups/tickets/group tickets/invoices/attractions, E3 typed attractions, passes, pass access, cascade refund, HELP EXTENDED) executed via `App.launch` with stdin, with a file, and with `--fly`; outputs compared; a second normal launch checks restored state; `--fly` leaves `data/` untouched.
- **JAR**: run the same script with `java -jar target/parque.jar` during verification (manual end-to-end step, recorded in the commit).
- **UML**: `UmlModelTest` and `plantuml -checkonly` must pass; update the model only if they reveal drift.

## Risks / Trade-offs

- [Placeholder parameters may trigger validation errors] → only `ERROR UNKNOWN COMMAND` is treated as failure.
