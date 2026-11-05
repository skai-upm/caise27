# Design

## Context

After F0-C01 the project is a quickstart-archetype Maven project (`es.upm.parque:amusement-park`, release 21) with `App.main` as the only class. `pom.xml` has only `pluginManagement`; `mvn package` creates `amusement-park-1.0-SNAPSHOT.jar` without a `Main-Class`.

## Goals / Non-Goals

**Goals:**
- `mvn package` creates `target/amusement-park.jar` with a `Main-Class` manifest entry.
- No runtime dependencies, so a plain JAR is self-contained.

**Non-Goals:**
- Command reading from stdin or from a file argument (later milestone F1 "running command sequences").
- Fat/shaded JARs: there are no runtime dependencies to bundle.

## Decisions

- **Manifest via `maven-jar-plugin` `archive.manifest.mainClass`** rather than the shade or assembly plugin. The project has only test-scoped dependencies, so the standard JAR plugin (already version-locked by the archetype) is enough. Shade adds a plugin and an extra artifact for no benefit now; it can be introduced if a runtime dependency appears.
- **`<finalName>amusement-park</finalName>`** so the launch command is stable across versions. The source's `nombre-aplicacion` is a placeholder for the application name.
- **Entry point `es.upm.parque.App`**: keep the archetype's class as the single entry point. Its behaviour stays minimal until the command-processing milestones define it.

## Risks / Trade-offs

- [The fixed final name hides the version in the file name] → The version stays in the pom; graders get a predictable command.
- [A runtime dependency added later would break `java -jar`] → Revisit packaging (shade) when the first runtime dependency appears.
