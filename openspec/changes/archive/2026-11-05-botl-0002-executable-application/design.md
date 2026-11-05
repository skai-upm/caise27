# Design

## Context

Baseline: quickstart project with `es.upm.amusementpark.App` (see proposal.md - Why). No runtime dependencies exist, so a plain (non-shaded) JAR is self-contained.

## Goals / Non-Goals

**Goals:**
- Stable JAR name and declared `Main-Class`.
- An input-reading session decoupled from `System.in` so that the future file-input milestone (F1-C07) can reuse the same processing with a different source.

**Non-Goals:**
- No command parsing or command set; lines read are not interpreted yet.
- No file-argument mode (F1-C07).

## Decisions

- **`maven-jar-plugin` manifest `mainClass`** rather than shade/assembly: no runtime dependencies, so a thin JAR is enough and keeps the build simple. Revisit if a runtime dependency is introduced.
- **`<finalName>amusement-park</finalName>`**: gives the deterministic `target/amusement-park.jar` path used in the assignment's `java -jar target/<name>.jar` examples.
- **Session reads from a `Reader`/`BufferedReader` passed in**: `App.main` wires `System.in`; tests pass a `StringReader`. Dependency direction: `App` → `InteractiveSession`; the session knows nothing about `System`.
- **Lines are consumed without output**: the source defines no command behavior for this milestone; leaving lines uninterpreted avoids inventing an error message format before F1-C09 defines controlled error handling.

## Risks / Trade-offs

- [Unrecognised input produces no feedback yet] → intentional; F1-C03+ add commands and F1-C09 controlled handling of invalid input.

## Open Questions

- Exact prompt/banner text is not specified by the source; none is printed for now.
