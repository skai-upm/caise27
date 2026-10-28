# Design

## Context

The repository currently contains only OpenSpec scaffolding, a README and a license; there is no Java code. See proposal.md for motivation. The installed JDK is newer than 21, so the build must pin the language level explicitly.

## Goals / Non-Goals

**Goals:**
- A quickstart-archetype Maven layout in the repository root that builds with `mvn package`.
- Java 21 language level enforced by the compiler configuration.

**Non-Goals:**
- No application behavior (main entry point, executable JAR, commands) — later milestones (F0-C02 onward) add those.

## Decisions

- **Generate with the real archetype** (`org.apache.maven.archetypes:maven-archetype-quickstart`, version 1.5) rather than hand-writing the files, so the structure is exactly what the archetype produces. Alternative: hand-craft an equivalent pom — rejected because the requirement names the archetype explicitly.
- **Generate into the repository root**: run the archetype in a temporary directory and move the generated project's contents to the root, because the archetype always creates a subdirectory named after the artifactId and the repository itself must be the project.
- **`maven.compiler.release=21`** instead of `source`/`target`: `release` also restricts the JDK API surface to Java 21, which is the stricter guarantee when building on a newer JDK.
- **Coordinates** `es.upm:amusement-park`, package `es.upm.amusementpark` (assumption, see proposal Impact).

## Risks / Trade-offs

- [Archetype download needs network access] → If the archetype cannot be resolved, reproduce its 1.5 output by hand and note the deviation.
- [Builds on JDK > 21 could hide 21-incompatibilities] → `release` flag makes javac reject APIs absent from Java 21.
