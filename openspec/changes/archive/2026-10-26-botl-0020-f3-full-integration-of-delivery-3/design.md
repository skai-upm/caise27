# Design

## Context

`App.buildProcessor(Path)` wires repositories, singleton controllers, the help generator and `CommandProcessor`; `CommandRunner` runs scripts. All features of E1–E3 are present. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Executable evidence that all features cooperate in every mode.

**Non-Goals:**
- New functionality.

## Decisions

- **Scripts in `examples/`**: each script is self-contained (creates its own users, sets `SYSTEM DATE`) so it runs on empty data and in `--fly`; `input_fase3.txt` includes E1/E2 setup plus every E3 command (typed attractions, passes, SPEED/SPECIAL entry, invoices, cascade deletion, HELP EXTENDED).
- **Integration test** (`es.upm.parque.AppIntegrationTest`): calls `App.buildProcessor(tempDir)` (package-private, same package) and a `CommandRunner` over each script file, asserting no output line contains `Unknown command` or `Wrong number of parameters` and checking key lines; then builds a second processor on the same directory (restart) and checks restored users, tickets, passes (consumed status) and attraction types.
- **Catalog completeness test**: for each catalog entry, processing its operation with no parameters must not yield `Unknown command`; and the list of operations dispatched by `CommandProcessor` is compared with the catalog by sending each operation name (an unregistered operation would fail).
- **JAR checks** are run manually and recorded in the commit: interactive (piped stdin), file mode, `--fly`, normal persistence.

## Risks / Trade-offs

- [Scripts depend on fixed dates] → each script sets `SYSTEM DATE` explicitly.
