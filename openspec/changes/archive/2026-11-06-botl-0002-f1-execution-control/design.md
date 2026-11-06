# Design

## Context

The application has only the archetype `App` class (see proposal.md). HELP and EXIT must behave identically for standard input and for a command file. The command file mechanism becomes a full feature in F1-C07, but a minimal version is needed now to satisfy and verify the EXIT file-mode rule.

## Goals / Non-Goals

**Goals:**
- One command-handling path, independent of the input source.
- Make EXIT a signal to the loop, not a `System.exit` call, so it can be tested and later commands are never read.

**Non-Goals:**
- Fixing a command framework for domain commands. Later milestones may add handlers.
- Full file-mode semantics (blank lines, error policy), which belong to F1-C07.

## Decisions

- **`CommandProcessor`** takes one line and returns a `CommandResult` (output text and a `terminate` flag). It is pure and has no I/O, so it is easy to unit-test. Alternative: handle commands inside the reading loop. Rejected because it mixes I/O with behavior and would lead to two code paths later.
- **`CommandRunner`** reads lines from a `BufferedReader`, writes each result to a `PrintStream`, and stops right after a result with `terminate=true` or at end of input. Both stdin and the file are adapted to a `BufferedReader`, so there is a single loop.
- **`App.main`**: with no arguments it reads `System.in`; with a first argument it opens that path as a UTF-8 file. If the file cannot be opened, it prints an error to stderr and returns (minimal handling, refined in F1-C07).
- **Command keyword matching** is on the trimmed line: `HELP` and `EXIT` must match exactly (case-sensitive, as written in the source).
- **HELP text** is a fixed list of the syntax lines mandated by the source, one per line.

## Risks / Trade-offs

- [HELP lists USER commands before they exist] → The source mandates this minimum content. USER commands are delivered by other milestones in this batch.
- [Case-sensitive keywords may reject `help`] → Matches the documented syntax. Relaxing this later is backward compatible.

## Open Questions

- The source does not state whether `HELP` should list only implemented commands or the full mandated set. This change shows the mandated set.
