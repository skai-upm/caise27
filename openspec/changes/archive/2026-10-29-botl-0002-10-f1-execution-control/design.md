# Design

## Context

The project is the quickstart skeleton (`App` prints "Hello World!"). Milestones that precede F1-C08 in the course numbering (interactive mode, command file input, USER commands) are not present in this repository yet; this change adds only what HELP/EXIT strictly need to be functional and testable from both sources.

## Goals / Non-Goals

**Goals:**
- One command processor shared by all input sources.
- HELP and EXIT with the source-required behavior; unknown lines rejected without stopping.

**Non-Goals:**
- USER CREATE/DELETE/UPDATE behavior, user model, validation and persistence.
- Executable JAR manifest (F0-C02) and full command-file rules such as blank-line handling guarantees (F1-C07) — beyond trimming input and skipping blank lines, which keeps the loop robust.

## Decisions

- **`CommandProcessor`** (package `es.upm.amusementpark`) owns dispatch. `boolean execute(String line)` runs one line and returns `false` when processing must stop (EXIT). `void run(BufferedReader in)` loops over lines until end of input or EXIT. Output goes to an injected `PrintStream` so tests can capture it. Alternative: separate loops for stdin and file — rejected, the source forbids divergent implementations.
- **`App.main`**: no args → `run` over `System.in`; first arg → open that file with a `BufferedReader` and `run` the same processor. An unreadable/nonexistent file prints an error message instead of a stack trace (controlled handling; detailed rules remain for F1-C07).
- **Matching**: trim the line; blank lines are ignored; keywords are matched exactly (`HELP`, `EXIT`).
- **HELP text**: one syntax per line, in the order given by the source.

## Risks / Trade-offs

- [Unknown-command and HELP wording are not specified] → documented as flagged ambiguity in the proposal; tests assert presence of syntaxes, not exact wording of messages.
- [Later milestones will reorganize into layers (F3)] → the processor is a small class that can be moved without behavior change.
