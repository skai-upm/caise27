# Design

## Context

Empty repository with OpenSpec scaffolding only. See proposal.md - Why. The local machine has Maven 3.9 running on JDK 24 and no JDK 21 installed.

## Goals / Non-Goals

**Goals:**
- Archetype-generated baseline at the repository root that compiles for Java 21, runs tests and packages into an executable JAR.

**Non-Goals:**
- Any domain classes, CLI parsing or persistence.
- Fat/shaded JARs (no runtime dependencies yet).

## Decisions

- **Generate with the archetype, then flatten to the root.** `mvn archetype:generate` creates a subdirectory named after the artifactId; its contents are moved to the repository root so the repo *is* the Maven project. Coordinates: groupId `es.upm.parque`, artifactId `parque`, package `es.upm.parque`.
- **`maven.compiler.release=21`** instead of `source/target`: `release` also checks API usage against JDK 21, so building with a newer JDK (24 locally) still guarantees JDK 21 compatibility.
- **Executable JAR via `maven-jar-plugin` manifest `mainClass`** (alternative: shade/assembly plugin). No dependencies are needed at runtime, so a plain manifest is enough. `finalName` set to `parque` for a stable `target/parque.jar` path.
- **Keep archetype's JUnit 5 setup** (the quickstart 1.5 archetype ships JUnit Jupiter via BOM) as the basic testing structure.

## Risks / Trade-offs

- [No JDK 21 locally] → `release=21` enforces the target API level; CI/lab JDK 21 builds identically.
- [Archetype version drift changes generated files] → pin archetype version 1.5 in the generate command.
