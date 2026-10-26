# Design

## Context

From botl-0002: `CommandProcessor.execute(String)` returns one response per line; `App.main` loops over `System.in`. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One read-execute loop used by both input sources; HELP/EXIT handled by `CommandProcessor` like any other command.

**Non-Goals:**
- Command pattern / per-command classes (explicitly deferred), new user features.

## Decisions

- **Shared loop `CommandProcessor.run(BufferedReader in, PrintStream out)`**: reads lines until EOF or until EXIT has been executed, printing each non-null response. `App` only decides which `BufferedReader` to build (`System.in` or `Files.newBufferedReader(path)`). Alternative: loop in `App` — rejected to keep the mode-independent behavior testable without spawning processes.
- **EXIT signalling**: `execute("EXIT")` returns `OK: Bye` and sets an `exitRequested` flag queried by `run`. Keeps `execute` returning a single response string (existing tests unchanged).
- **HELP text**: a constant list of usage lines held in `CommandProcessor`, reusing the same usage strings that parameter-count errors already show, so help and errors cannot drift apart.
- **File errors**: `App` catches `IOException` (covers `NoSuchFileException`, access denied, and read failures surfacing as `UncheckedIOException` are also caught) and prints `ERROR: Cannot read command file <path>: <reason>`; `main` returns normally with no stack trace. Exit status remains 0 for simplicity (not specified).
- **Single-word operations**: the head of the `;` split is split by space; one token → HELP/EXIT, two tokens → domain/action.

## Risks / Trade-offs

- [EXIT inside a file leaves later lines unread] → required behavior.
- [Persistent user file shared by file and interactive runs] → unchanged from botl-0002.
