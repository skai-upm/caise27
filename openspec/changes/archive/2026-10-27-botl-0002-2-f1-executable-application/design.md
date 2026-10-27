# Design

## Context

The project is the plain quickstart build from F0-C01 (`es.upm.amusementpark.App`). No command language exists yet.

## Goals / Non-Goals

**Goals:**
- Runnable fat-free JAR (no dependencies at runtime) at `target/nombre-aplicacion.jar`.
- Single entry point that selects the input source: stdin or a file.

**Non-Goals:**
- Parsing or executing commands, prompts, help/exit (F1 milestones).

## Decisions

- **`maven-jar-plugin` manifest `mainClass`** instead of shade/assembly: the application has no runtime dependencies, so a plain JAR with `Main-Class` is sufficient.
- **`<finalName>nombre-aplicacion</finalName>`** so the documented command works literally.
- **New `Main` class** as entry point; it opens a `BufferedReader` on `System.in` or on the file (UTF-8) and passes each line to a line handler. For now the handler does nothing; later milestones plug the command processor in. The archetype `App` class is removed to avoid two competing entry points (its trivial test goes with it).
- **Testability**: `Main.run(String[] args, InputStream in, PrintStream err)` returns an exit code; `main` calls `System.exit` only on non-zero codes. Tests drive `run` directly.

## Risks / Trade-offs

- [Interactive mode cannot be observed as "waiting" in a unit test] → tested by reading from a supplied stream until EOF; manual check with `java -jar` in a terminal.
