# Design

## Context

`App.run(BufferedReader, PrintStream)` already loops over lines, skips blank ones and delegates to `CommandProcessor.process(String)`, which returns the response text. `main` always wires `System.in`. Usage strings for the USER commands live in `CommandProcessor.requireParams` calls.

## Goals / Non-Goals

**Goals:**
- One processing path: both modes only differ in which `BufferedReader` is passed to `run`.
- HELP built from the same syntax strings used to report wrong parameter counts, so they cannot drift.

**Non-Goals:**
- Command registries, the Command pattern, or any other structure intended for later deliveries.

## Decisions

- **Input source selection in `main`**: `args.length == 0` → `BufferedReader` over `System.in`; otherwise open `Path.of(args[0])` with `Files.newBufferedReader(path, UTF_8)` in try-with-resources. Extra arguments are ignored. Alternative: a separate file-runner class — rejected, it would duplicate the loop.
- **Invalid file handling**: before opening, `Files.notExists` → `ERROR Input file not found: <path>`; `NoSuchFileException`/`AccessDeniedException`/other `IOException` while opening (including a directory) → `ERROR Cannot read input file: <path>`. A read failure mid-file is already reported by `run`'s catch. `main` simply returns — no `System.exit`, no stack trace.
- **EXIT signalling**: `CommandProcessor` sets an `exitRequested` flag when it processes `EXIT` and exposes `isExitRequested()`; `run` checks it after printing each response and breaks the loop. Alternative: return a result object from `process` — rejected as a larger API change for one boolean.
- **HELP text**: `CommandProcessor` keeps the syntax strings as constants in a list `COMMAND_SYNTAX`; HELP returns `OK Available commands:` followed by one syntax per line. `requireParams` reuses the same constants.
- **Responses**: `EXIT` → `OK EXIT`. `HELP`/`EXIT` with parameters → wrong-parameter-count error, like other commands.

## Risks / Trade-offs

- [Stateful processor flag] → Only read by `App.run`; reset is unnecessary because the application terminates after EXIT.
