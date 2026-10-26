# Design

## Context

Each Delivery 3 feature has its own unit and command tests; `AppTest`/`FlyModeTest` cover the modes with E1/E2 commands. No single scenario combines everything.

## Goals / Non-Goals

**Goals:**
- One realistic script, kept in the repository, used both as documentation of file mode and as the integration test input.

**Non-Goals:**
- New features or refactorings.

## Decisions

- **`examples/input_fase3.txt`**: ~50 commands: HELP / HELP EXTENDED, three users (one password change), a group, SYSTEM DATE/PRICE, individual and group tickets, SpeedPasses (1 and 10) and a Special Pass, one refund, attractions of the four types, entries with and without SPEED including expected rejections (wrong type, SPEED at SPECIAL, member without SpeedPass), status, exits, user and group invoices, a lock rejection, and EXIT followed by a line that must not run. It uses the default system-date-independent commands (`SYSTEM DATE` set explicitly at the start) so its outcome is deterministic.
- **`Delivery3IntegrationTest`** (in `es.upm.parque`): reads the script from `examples/`, runs it through `App.execute` with a fixed clock in file mode, interactive mode and `--fly`, and asserts: the list of `OK`/`ERROR` first-words per command equals an expected sequence written in the test; interactive output equals file output; after a normal run a restart authenticates the changed password, keeps consumed passes and attraction types; the `--fly` run produces identical output and leaves no `data/` directory.
- Help coverage: every `CommandProcessor.COMMANDS` syntax appears in both help outputs (already partially tested; consolidated here for the new commands).
- JAR and UML: verified by running the packaged JAR on the script and the PlantUML/cross-check scripts during apply.

## Risks / Trade-offs

- [Expected sequence must be updated if outputs change later] → Intended: it pins the integrated behaviour of the delivery.
