# Design

## Context

The repository currently contains only `README.md`, `LICENSE`, `.gitignore` and the `openspec/` tree. No Java code exists. Maven 3.9.11 is installed; the local JDK is newer than 21, so the build must pin the language level with `maven.compiler.release=21` rather than relying on the installed JDK version.

## Goals / Non-Goals

**Goals:**
- Reproduce the structure that `mvn archetype:generate -DarchetypeArtifactId=maven-archetype-quickstart` produces.
- Pin the compiler to Java 21 and keep `mvn package` green.

**Non-Goals:**
- Executable JAR configuration (later milestone F0-C02).
- Any domain model or command handling (Delivery 1 later milestones).

## Decisions

- **Generate with the official archetype** (`org.apache.maven.archetypes:maven-archetype-quickstart`, version 1.5) instead of hand-writing files, so the structure is exactly the archetype's. Alternative: hand-written pom — rejected, risks deviating from the mandated structure.
- **Coordinates**: groupId `es.upm.etsisi.amusementpark`, artifactId `amusement-park`, package `es.upm.etsisi.amusementpark`. The source does not specify coordinates; this is an assumption recorded here, not a business rule.
- **Java level**: set `maven.compiler.release` to `21`. Alternative: `source`/`target` — `release` is stricter (also validates the API surface).
- **Test framework**: keep the archetype's JUnit 5 (Jupiter) dependency.

## Risks / Trade-offs

- [Archetype download requires network access] → If unavailable, reproduce the archetype 1.5 output by hand with identical layout.
- [Installed JDK newer than 21] → `release=21` guarantees Java 21 bytecode and API compatibility.
