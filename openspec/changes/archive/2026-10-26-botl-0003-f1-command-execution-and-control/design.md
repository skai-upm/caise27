# Design

## Context

`App.main` currently wires `UserRepository → UserManager → CommandProcessor` and loops over `System.in`. `CommandProcessor.process(String)` returns the response string. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One command loop used by both modes; mode selection limited to choosing a `BufferedReader`.
- HELP derived from a single list of command syntaxes so it is easy to extend.

**Non-Goals:**
- Command objects / command pattern (later delivery).
- Echoing input commands in file mode.

## Decisions

- **`CommandRunner` class** owns the shared loop: `run(BufferedReader)` reads lines, skips blank ones, calls `CommandProcessor.process`, prints the response to an injected `PrintStream`, and stops when the processor reports that `EXIT` was processed. `App` only builds the objects and picks `System.in` or `Files.newBufferedReader(Path.of(args[0]))`. Alternative: keep the loop in `App` with an `if` per mode — rejected because it invites duplicated loops and is hard to test.
- **EXIT state lives in `CommandProcessor`** (`isExitRequested()`), set when `EXIT` is processed; the response is `OK EXIT`. Keeps all command semantics in one place.
- **HELP** returns `Available commands:` followed by one syntax per line from a constant list in `CommandProcessor` (the same strings used for the usage hints of parameter-count errors).
- **File errors**: `IOException` (including `NoSuchFileException`) while opening or reading the file is caught in `App`/`CommandRunner` and reported as `ERROR Cannot read command file '<path>': <reason>`; `main` returns normally.

## Risks / Trade-offs

- [EXIT in the middle of a file leaves later lines unread] → intended behavior per spec.
- [An I/O error mid-file stops processing after some commands ran] → reported with `ERROR`; already executed commands remain applied (same as interactive).
