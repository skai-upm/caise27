# Design

## Context

The project is the quickstart scaffold from F0-C01 (`es.upm.etsisi.amusementpark.App` printing "Hello World!"). No command handling exists. Later milestones add USER commands, file-mode execution and an executable JAR, so the processor must be reusable for any line source.

## Goals / Non-Goals

**Goals:**
- A single command processor working on a `BufferedReader` and a `PrintStream`, so stdin and file contents share exactly the same code path.
- HELP and EXIT implemented and unit-tested.

**Non-Goals:**
- USER commands, file-mode invocation from the command line, executable JAR packaging (later milestones).

## Decisions

- **`CommandProcessor` class** (`es.upm.etsisi.amusementpark.cli`) with `run(BufferedReader in, PrintStream out)`: reads line by line, trims, skips blank lines, dispatches. Returns when EXIT is processed or input ends. Alternative: a `Scanner` loop inside `main` — rejected, not testable and would duplicate logic for file mode.
- **Command dispatch** via a small `Command` interface keyed by keyword (first token before `;`), so later USER commands register without changing the loop.
- **EXIT** sets a stop flag checked before reading the next line; no `System.exit` inside the processor (controlled termination; the entry point simply returns).
- **Case-sensitive keywords**: command names are contractual (`HELP`, `EXIT`).
- **Unknown command**: prints `ERROR: Unknown command: <line>` and continues.

## Risks / Trade-offs

- [HELP lists USER commands before they are implemented] → Required verbatim by the source; flagged in proposal.
- [Blank-line skipping is an assumption] → Harmless; blank lines carry no command.
