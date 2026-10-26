# Design

## Context

The repository contains only OpenSpec scaffolding, a README and a LICENSE (see proposal.md - Why). The local toolchain is Maven 3.9.x running on a JDK newer than 21, so the build must explicitly target release 21 rather than rely on the running JDK version.

## Goals / Non-Goals

**Goals:**
- Generate the project with the real quickstart archetype instead of hand-writing an equivalent layout.
- Produce `target/parque.jar` runnable with `java -jar`.

**Non-Goals:**
- Fat/shaded JARs, dependency management for runtime libraries, CI configuration.

## Decisions

- **Generate with `mvn archetype:generate` (archetype 1.5) in batch mode** into a temporary directory and move the result to the repository root, so existing repository files (README, LICENSE, openspec/) are untouched. Alternative: generate in place with `-DinteractiveMode=false` — rejected because the archetype creates a subdirectory named after the artifactId.
- **Coordinates**: `groupId=es.upm.parque`, `artifactId=parque`, package `es.upm.parque`. The artifactId matches the JAR name used by later deliveries (`target/parque.jar`).
- **Java 21 via `maven.compiler.release=21`** instead of `source`/`target`, so the compiler also checks API usage against the Java 21 platform even when Maven runs on a newer JDK.
- **Executable JAR via `maven-jar-plugin` manifest `mainClass`** and `<finalName>parque</finalName>`. Alternative: shade/assembly plugin — unnecessary with no runtime dependencies.
- **Keep the archetype's JUnit 5 test** (`AppTest`) as the basic testing structure.

## Risks / Trade-offs

- [Archetype version changes generated pom contents] → Pin archetype version 1.5 in the generation command.
- [Running JDK differs from 21] → `release` flag guarantees Java 21 bytecode and API compatibility.
