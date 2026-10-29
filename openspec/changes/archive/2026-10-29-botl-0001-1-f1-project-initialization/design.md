# Design

## Context

The repository root holds only OpenSpec configuration, a README and a LICENSE. The Maven project is created at the repository root so `mvn package` works from there and later milestones can reference `target/<jar>`. The local toolchain has a newer JDK (24/25) than the required 21; Maven's `release` option makes the build produce JDK 21 bytecode and restricts the API to Java 21 regardless.

## Goals / Non-Goals

**Goals:**
- A quickstart-archetype Maven project at the repository root, compiling for Java 21.
- `mvn package` succeeds and runs the sample test.

**Non-Goals:**
- Executable JAR / entry-point manifest (milestone F0-C02).
- Any domain model, commands or persistence.

## Decisions

- **Generate with the real archetype** (`org.apache.maven.archetypes:maven-archetype-quickstart`, current 1.5 release) in batch mode, rather than hand-writing the files, so the structure is exactly the archetype's. Alternative: hand-craft an equivalent pom; rejected because the source requires the archetype.
- **Coordinates (assumption, flagged):** the source text fixes no groupId/artifactId/package. We use `groupId=es.upm.amusementpark`, `artifactId=amusement-park`, root package `es.upm.amusementpark`. The source's examples use `target/nombre-aplicacion.jar` as a placeholder name; the jar name is not contractual at this milestone.
- **`maven.compiler.release=21`** instead of separate source/target, because `release` also enforces the Java 21 API surface.
- **Test framework:** keep the archetype's JUnit Jupiter setup (test-scoped only; no runtime libraries are added).
- **Mandatory milestone commit message:** the batch orchestrator mandates its own commit subject; the exact milestone message `[F0-C01] Initialize Maven project with JDK 21` is included verbatim in the commit body so it is searchable in history.

## Risks / Trade-offs

- [Archetype download needs network access] → If generation fails offline, fall back to reproducing the archetype's files exactly and note it.
- [Local JDK newer than 21] → `release=21` guarantees Java 21 compatibility; verified by checking class-file major version 65.
