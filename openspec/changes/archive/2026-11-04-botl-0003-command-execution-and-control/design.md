# Design

## Context

`App.main` currently builds `UserFileRepository` → `UserManager` → `CommandProcessor` and runs a stdin loop inline. `CommandProcessor.process(String)` returns one result string (or `null` for blank lines) and never throws. The users file is `users.txt` in the working directory.

## Goals / Non-Goals

**Goals:**
- One read-process loop used for both input sources; one place where commands are interpreted.
- `HELP` text generated from a single list that later deliveries extend.
- File problems end the run with an `ERROR` line, never a stack trace.

**Non-Goals:**
- No changes to user rules, model or persistence.
- No command history, prompt, or echo of commands (it would make interactive and file output differ).

## Decisions

- **`CommandRunner` owns the loop.** `run(BufferedReader in, PrintStream out)` reads lines until EOF or until the processor reports that `EXIT` was executed, printing each non-null result. Both modes call it, so EXIT, blank-line skipping and ordering behave identically. An `IOException` while reading (for example a `MalformedInputException` from invalid UTF-8) is caught there, reported as `ERROR: Could not read commands: ...`, and ends the loop. Alternative considered: an `InputSource` interface with two implementations. Rejected because a `BufferedReader` already abstracts both sources.
- **`HELP` and `EXIT` live in `CommandProcessor`.** They are dispatched in the same `switch` as user commands. `EXIT` sets an `exitRequested` flag exposed through `isExitRequested()`. `HELP` returns `OK: Available commands:` plus one indented syntax line per command, taken from a single `List<String>` constant (`HELP_LINES`) that later deliveries extend. Both reject extra parameters.
- **Mode selection in `App`.** `App.run(String[] args, InputStream stdin, PrintStream out, Path usersFile)` returns an exit status. With no args it wraps `stdin`; otherwise it opens `args[0]` with `Files.newBufferedReader(path, UTF_8)`. `NoSuchFileException`, `AccessDeniedException`, other `IOException`s (including a directory path) and `InvalidPathException` are reported as `ERROR: Cannot open command file '<path>': <reason>` with status 1. `main` delegates to `run` with the real streams and `users.txt`, and calls `System.exit` only for a non-zero status. Extra arguments after the first are ignored (assumption: the source defines only the first argument).
- **Errors go to standard output.** `ERROR` lines for file problems are printed on the same stream as command results, so a user sees them in the same place. Assumption: there is no requirement to separate the streams.

## Risks / Trade-offs

- [`EXIT` inside a file leaves the rest unread] → intended by the spec.
- [Interactive users get no prompt] → keeps output identical across modes, as the equivalence requirement demands.
