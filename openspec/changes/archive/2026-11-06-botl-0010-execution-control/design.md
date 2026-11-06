# Design

## Context

See proposal.md – Why. All input goes through `Main.run(processor, reader, out)`, which calls `CommandProcessor.process(line)` per line.

## Goals / Non-Goals

**Goals:**
- Exit handled once, in the shared loop, so both input modes behave identically.

**Non-Goals:**
- Calling `System.exit` (would stop tests and skip normal shutdown).

## Decisions

- **Exit signal**: `CommandProcessor` keeps a `boolean exitRequested`, set by `EXIT`, exposed via `isExitRequested()`. `Main.run` checks it after printing each result and stops reading. Alternative: return a result object from `process` — larger API change for one flag.
- **HELP text**: a constant multi-line string listing the five syntaxes plus the modifiable fields from `UserField.values()`, so the help stays in sync with the enum.
- **Field count**: `HELP`/`EXIT` expect 0 fields; reuse the existing `expects N fields` error.
- **Closing message**: `Bye`.

## Risks / Trade-offs

- [Lines after EXIT on stdin are left unread] → intended: the application ends.
