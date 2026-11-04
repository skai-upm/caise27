# Design

## Context

The repository root holds only `README.md`, `LICENSE`, `.gitignore` (already ignoring `target/`) and `openspec/`. No build exists yet. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Archetype-generated layout at the repository root so later changes use plain `mvn` commands.
- Deterministic Java 21 compilation and an executable JAR.

**Non-Goals:**
- Fat/uber JARs, shading, extra plugins, logging frameworks or CI configuration.

## Decisions

- **Generate with the archetype, then adjust in place.** Run `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DgroupId=es.upm.park -DartifactId=amusement-park -DinteractiveMode=false` in a temporary directory and move its contents to the repository root. Alternative (hand-writing the files) rejected: the source explicitly requires the archetype.
- **Java level via `maven.compiler.release=21`.** `release` (not `source`/`target`) also restricts the API surface to Java 21. Alternative of a toolchains file rejected as unnecessary infrastructure; the installed JDK (≥21) is used with `--release 21`.
- **Executable JAR via `maven-jar-plugin` manifest `mainClass=es.upm.park.App`.** No runtime dependencies exist, so a plain JAR suffices; assembly/shade plugins rejected as unneeded.
- **Keep archetype tests (JUnit 5 in recent archetype versions).** They form the basic testing structure; Surefire runs them in `mvn test`.

## Risks / Trade-offs

- [Installed JDK newer than 21] → `--release 21` guarantees Java 21 bytecode and API; verified by class-file major version 65.
- [Archetype version produces JUnit 4 or 5] → whichever the archetype generates is kept; verification only requires tests to run and pass.
