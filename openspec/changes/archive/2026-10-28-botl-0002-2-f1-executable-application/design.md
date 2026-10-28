# Design

## Context

The project is the bare quickstart archetype: `App.main` prints "Hello World!" and the JAR has no `Main-Class` manifest entry. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Runnable JAR with a fixed name and `Main-Class` manifest entry.
- One input loop shared by interactive and file modes, testable without spawning processes.

**Non-Goals:**
- Any concrete command (help, exit, user commands) — later milestones.

## Decisions

- **`maven-jar-plugin` manifest `mainClass`** set to `es.upm.amusementpark.App`, and `<finalName>amusement-park</finalName>`. No shade/assembly plugin: the project has no runtime dependencies, so a plain JAR is enough.
- **`App.run(BufferedReader, PrintStream out, PrintStream err)`** contains the loop; `main` only selects the reader (stdin or file) and maps failures to an exit code. Unit tests call `run`/`execute` with in-memory streams. Alternative: a separate `CommandProcessor` class now — deferred until commands exist to avoid speculative structure.
- **Line handling hook**: each non-null line goes to a `processLine` method that currently does nothing; later milestones replace its body with command dispatch.
- **Missing file**: `main` prints `Error: cannot read file '<path>'` to stderr and exits with status 1 (flagged ambiguity, see proposal).

## Risks / Trade-offs

- [JAR name differs from the placeholder in the statement] → Documented; the name is configurable in one place (`finalName`).
