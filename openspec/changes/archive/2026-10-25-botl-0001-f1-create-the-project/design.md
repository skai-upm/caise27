# Design

## Context

The repository only contains OpenSpec scaffolding, a README and a LICENSE. The local toolchain provides Maven 3.9 running on a JDK newer than 21, so the build must pin the language level explicitly. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- A quickstart-archetype project at the repository root that compiles for Java 21, runs tests and packages an executable JAR.

**Non-Goals:**
- Any domain code (users, attractions, tickets…), extra dependencies or plugins beyond what packaging an executable JAR needs.

## Decisions

- **Generate with the real archetype, then place it at the repository root.** `mvn archetype:generate -DarchetypeArtifactId=maven-archetype-quickstart` is run in a temporary directory with `groupId=es.upm.parque`, `artifactId=parque`, `package=es.upm.parque`; the generated `pom.xml` and `src/` are moved to the root so later epics build with a plain `mvn` from the root. Alternative: keep a `parque/` sub-directory — rejected because every command and tool would need an extra path.
- **artifactId `parque` with `finalName` `parque`.** Later proposals refer to `target/parque.jar`; fixing the name now avoids version-suffixed JAR names.
- **Java level via `maven.compiler.release=21`.** `release` (rather than `source`/`target`) also checks API usage against Java 21 when built on a newer JDK.
- **Executable JAR via `maven-jar-plugin` manifest `mainClass`.** No dependencies exist yet, so a fat/shaded JAR is unnecessary. Alternative: shade plugin — deferred until a runtime dependency is introduced.
- **Keep the archetype's `App`/`AppTest`.** The archetype's JUnit version and test layout are retained; `App` keeps printing a minimal greeting.

## Risks / Trade-offs

- [Archetype download needs network access] → If generation fails offline, stop and report instead of hand-writing an imitation.
- [Archetype defaults to an older Java level] → Overridden by `maven.compiler.release=21` in `pom.xml`.
