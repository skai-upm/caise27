# Design

## Context

The repository contains only OpenSpec scaffolding, a README and a LICENSE. The build machine has Maven 3.9.11 and JDK 24/25 installed, but **no JDK 21**. The archetype `maven-archetype-quickstart` is already in the local Maven repository.

## Goals / Non-Goals

**Goals:**
- Generate the skeleton with the archetype at the repository root so that later changes extend one Maven module.
- Java 21 compilation, executable JAR, passing `mvn test`.

**Non-Goals:**
- Any domain, command or persistence code.
- Extra plugins (shade/assembly, coverage, linters) beyond what an executable JAR requires.

## Decisions

- **Generate with the archetype, then adjust the POM.** Run `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart` in batch mode with `groupId=es.upm.etsisi`, `artifactId=amusement-park`, `package=es.upm.etsisi.amusementpark`, then move the generated content to the repo root. Alternative (hand-writing the POM) was rejected because the source explicitly requires the archetype.
- **Java 21 via `maven.compiler.release=21`.** `release` (instead of `source`/`target`) also restricts the API to Java 21, so building on a newer JDK still yields Java 21-compatible output. This is how the requirement is satisfied on this machine, which lacks JDK 21.
- **Executable JAR via `maven-jar-plugin` manifest `mainClass`.** The project has no runtime dependencies, so a fat JAR (shade/assembly) is unnecessary. Alternative rejected: shade plugin adds infrastructure the source does not require.
- **Keep the archetype's generated `App` and `AppTest`.** Entry point: `es.upm.etsisi.amusementpark.App`. Test framework: the JUnit version declared by the archetype (JUnit 5 in current archetype versions).

## Risks / Trade-offs

- [No JDK 21 locally] → `release=21` guarantees Java 21 bytecode and API; verified via class-file major version 65. A JDK 21 run remains to be done by whoever has one.
- [Archetype version drift changes generated files] → the generated output is committed, so the skeleton is reproducible from Git.
