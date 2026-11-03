# Design

## Context

`App.run(InputStream, PrintStream, Path)` wires `UserRepository("users.txt") → UserService → CommandProcessor` and loops over standard-input lines, skipping blank ones. `CommandProcessor.process(String)` returns the response text and already contains all `USER` command logic. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One loop and one processor for both input sources; the source is just a `BufferedReader`.
- `EXIT` stops the loop regardless of the source.

**Non-Goals:**
- Any change to user model, validation or persistence; prompts or colour output; multiple input files.

## Decisions

- **Mode selection in `App`:** `run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path usersFile): int` returns the exit status. With `args.length == 0` it wraps `stdin`; otherwise it opens `Files.newBufferedReader(Path.of(args[0]))`. `main` calls `run` and, only when the status is non-zero, calls `System.exit(status)`. Returning the status (instead of exiting inside `run`) keeps the behaviour testable.
- **Shared loop:** private `processLines(BufferedReader, PrintStream, CommandProcessor)` reads a line, skips blank lines, prints `processor.process(line)`, and breaks as soon as `processor.isExitRequested()` is true. Used for both modes — no duplicated command logic.
- **EXIT signalling:** `CommandProcessor` keeps a boolean set by `EXIT`; alternative of returning a result record was rejected to avoid changing every caller and test of `process(String)`.
- **HELP text:** a single ordered `List<String>` of syntax lines in `CommandProcessor` (`COMMAND_SYNTAX`); `HELP` returns `"Available commands:"` + one line per entry. Adding a command later means adding its line there.
- **General command parsing:** after `split(";", -1)`, if the first token is exactly `HELP` or `EXIT` it is handled before `USER` dispatch; any extra parameter makes it a wrong-parameter-count error.
- **File errors:** opening and reading are wrapped in `try (BufferedReader …)` catching `IOException` (covers `NoSuchFileException`, `AccessDeniedException`, directory reads, malformed input) and `InvalidPathException`; the message `ERROR: Cannot read command file '<path>': <reason>` goes to `err`, status 1. A failure while loading `users.txt` at startup is handled the same way (status 1) so no stack trace is ever shown.

## Risks / Trade-offs

- [`System.exit` hides the status from in-JVM tests] → tests call `run(...)` and assert the returned status; only `main` exits.
- [Partial execution before a mid-file read error] → acceptable and documented: already-executed commands were valid and persisted, as in interactive mode.
