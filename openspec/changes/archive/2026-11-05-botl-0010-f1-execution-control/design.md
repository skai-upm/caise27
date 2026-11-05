# Design

## Context

`App.start` selects stdin or a file and calls `App.run(processor, reader, out)`, which loops over all lines. `CommandProcessor.process(line)` returns a response string or `null`.

## Goals / Non-Goals

**Goals:**
- `EXIT` stops the shared loop, so both sources behave identically.

**Non-Goals:**
- `System.exit`. Returning from `main` gives a clean termination.

## Decisions

- **Exit state in `CommandProcessor`** (`isExitRequested()`), set when `EXIT` is processed. `App.run` checks it after printing each response and breaks out of the loop. Commands stay pure string-in/string-out, and the loop owns reading, so lines after `EXIT` are never even read.
- **`HELP` returns one multi-line string** built from a constant. It lists the five commands from §11 and the `USER UPDATE` field names. Keeping it in `CommandProcessor`, next to the command names, keeps it consistent as commands are added.
- **Strict element count**: `HELP` and `EXIT` take no elements. `HELP;x` or `EXIT;x` answer `ERROR`, consistent with the other commands' element checks. A malformed `EXIT` therefore does not exit.
- **`EXIT` response** `OK: exiting application`.

## Risks / Trade-offs

- [A malformed `EXIT;x` keeps the application running] → Consistent with the other commands' strict parsing and reported as `ERROR`.
