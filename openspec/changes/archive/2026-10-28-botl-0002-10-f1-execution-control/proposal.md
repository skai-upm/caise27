# Proposal

## Why

Milestone F1-C08 of Amusement Park Delivery 1 requires the general `HELP` and `EXIT` commands: `HELP` lists the available commands and their basic syntax, and `EXIT` terminates the application in a controlled manner so that no later command runs, with identical behaviour whether commands arrive on standard input or from a file. The project currently only contains the quickstart placeholder application, so a command-processing loop is needed to host both commands.

## What Changes

- Introduce a line-oriented command processor that reads commands one per line from an input source and dispatches them.
- Add the `HELP` command, which prints the mandated command syntax list.
- Add the `EXIT` command, which stops processing: no subsequent line from the same source is executed.
- Wire the application entry point to run the processor on standard input.
- Commit with the exact message `[F1-C08] Add help and exit commands`.

## Capabilities

### New Capabilities
- `execution-control`: General commands controlling the application session: listing available commands (`HELP`) and controlled termination (`EXIT`), with identical semantics for any line-based input source.

### Modified Capabilities
<!-- none -->

## Impact

- New production code in the `es.upm.etsisi.amusementpark` package tree (command processor, entry point update) and unit tests.
- Flagged ambiguities:
  - The source says HELP shows "the operations currently available", yet requires at minimum the `USER CREATE/DELETE/UPDATE` syntax, which are later milestones. The mandated minimum list is printed verbatim as contractual text.
  - Reading commands from a file (file mode) is a later milestone; this change makes the processor source-agnostic and verifies the EXIT rule over a file-like line sequence, without adding file-mode invocation.
  - Handling of unrecognised commands is not specified; they are reported with an error message and processing continues (no new business rule beyond reporting).
