# Design

## Context

`App.main` currently wraps `System.in` in a `BufferedReader` and sends every non-blank line to `CommandProcessor`, which handles `USER CREATE|DELETE|UPDATE` and returns a `Result` record (`success`, `message`). See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One reading loop and one command processor used by both input sources; EXIT and blank-line handling implemented once.
- Startup and file errors reported as messages and an exit status, never as stack traces.

**Non-Goals:**
- Changes to user rules or persistence; command history, prompts, or scripting features; commands from later deliveries.

## Decisions

- **`Result` gains a `terminate` component** (`record Result(boolean success, String message, boolean terminate)`), with the existing two-argument `ok`/`error` factories unchanged and a new `Result.exit(message)`. The processor decides what EXIT means; the loop only reacts to the flag. Alternative (loop checks the literal text `EXIT`) rejected: it would duplicate command interpretation outside the processor.
- **`CommandRunner.run(BufferedReader in, PrintStream out)`**: reads lines, skips blank ones, prints `processor.process(line)` and stops when the result has `terminate`. Both modes call it; only the reader differs (`System.in` vs `Files.newBufferedReader(path)`). An `IOException` while reading propagates to `App`, which reports it.
- **`App.run(String[] args, InputStream stdin, PrintStream out, PrintStream err)` returns an exit status**; `main` calls it and invokes `System.exit` only for a nonzero status. This makes both modes testable in-process. Status 0 = normal end (EXIT or end of input); 1 = command file or user data file could not be opened/read. Errors print `ERROR: Cannot read command file '<path>': <reason>` to `err`.
- **HELP text** lives in `CommandProcessor` as an ordered list of syntax lines, one per command, so later deliveries add a line next to the command they add. `HELP` and `EXIT` take no parameters; extra parameters produce a usage error.
- **No prompt/echo** in interactive mode, so the output of a command sequence is byte-identical in both modes (supports the equivalence requirement).

## Risks / Trade-offs

- [A malformed `.txt` encoding in the command file] → read as UTF-8; a `MalformedInputException` is an `IOException` and is reported like any read error.
- [`System.exit` in `main`] → only on failure, after all output is flushed; tests call `App.run` instead.
