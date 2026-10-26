# Design

## Context

`App.main` currently loops over standard input and delegates each line to `CommandProcessor.process`, which returns an `OK`/`ERROR` line (see `openspec/specs/command-interface`). No design patterns are to be introduced yet.

## Goals / Non-Goals

**Goals:**
- One loop and one processor serve both input sources.
- Behaviour testable without spawning a JVM.

**Non-Goals:**
- Command objects/registries (later deliveries), changes to the user model.

## Decisions

- **Mode selection in `App`**: `main(args)` delegates to `App.run(String[] args, InputStream stdin, PrintStream out, Path usersFile)`. With no arguments it wraps `stdin`; with an argument it opens the file via `Files.newBufferedReader`. Both readers go to the same private `processLines(BufferedReader, CommandProcessor, PrintStream)` loop. Alternative: two loops — rejected (duplication forbidden).
- **EXIT signalling**: `CommandProcessor` sets an `exitRequested` flag when it processes `EXIT` and returns `OK EXIT`; the loop stops as soon as `isExitRequested()` is true. Alternative: returning a sentinel string — more fragile.
- **HELP text** is built in `CommandProcessor` from a single list of command syntaxes so later deliveries append to one place.
- **Blank lines** are skipped in the shared loop (`line.isBlank()`).
- **Errors**: `NoSuchFileException`, `AccessDeniedException` and other `IOException`s (including undecodable content raised while reading) are caught in `run` and reported as `ERROR: ...` with the file path; `main` never lets them propagate.
- **Parameterless commands**: `HELP` and `EXIT` must have no parameters (`HELP;x` is a parameter-count error).

## Risks / Trade-offs

- [`EXIT` inside a file also ends the program] → required by the spec.
- [Files in a non-UTF-8 encoding fail to decode] → reported as a controlled read error.
