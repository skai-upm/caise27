# Design

## Context

`App.main` wires `UserRepository` → `UserManager` → `CommandProcessor` and calls `App.run(BufferedReader, PrintStream, CommandProcessor)`, which loops over lines until end of input and prints `processor.process(line)` for non-blank lines. `CommandProcessor` dispatches with a `switch` on the upper-cased operation name.

## Goals / Non-Goals

**Goals:**
- One processing path for both modes: the mode only decides which `BufferedReader` is given to `run`.

**Non-Goals:**
- Command registry abstractions or design patterns (planned for later deliveries), new user features, `--fly`.

## Decisions

- **Mode selection in `App`.** `App.start(String[] args, InputStream stdin, PrintStream out)` chooses the source: no arguments → `stdin`; otherwise `Files.newBufferedReader(Path.of(args[0]))`. `main` only delegates to `start` with the real streams, so the whole flow is testable. Alternative: separate `InteractiveRunner`/`FileRunner` classes — rejected as unnecessary duplication of the same loop.
- **`EXIT` handled by `CommandProcessor`.** It returns `OK EXIT` and sets a `finished` flag exposed by `isFinished()`; `App.run` stops reading as soon as the flag is set. This keeps EXIT semantics identical in both modes and keeps termination out of the business classes.
- **`HELP` text.** `CommandProcessor` keeps an ordered list of command syntaxes next to the `switch` and joins them with the line separator. Keeping both in the same class makes forgetting to document a new command visible during review.
- **File errors.** Opening (`NoSuchFileException`, `AccessDeniedException`, …) and reading (`IOException` / `UncheckedIOException`) failures are caught in `start` and reported as `ERROR CANNOT READ FILE <path>` using the argument as given; `main` never lets an exception escape. Malformed UTF-8 is a read error (`MalformedInputException` is an `IOException`).
- **Extra arguments** beyond the first are ignored (assumption; the proposal only defines the first argument).

## Risks / Trade-offs

- [Help list and switch could drift apart] → `CommandProcessorTest` checks that every documented command is recognized (none returns `ERROR UNKNOWN COMMAND`).
- [Error on user-data loading at startup] → Still reported as `ERROR <message>` as before; unchanged.
