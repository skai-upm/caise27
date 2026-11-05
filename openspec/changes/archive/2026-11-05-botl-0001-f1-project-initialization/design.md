# Design

## Context

Greenfield repository (only LICENSE, README, `.gitignore` already ignoring `target/`). See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Produce the exact structure the quickstart archetype generates, so later milestones build on an accepted layout.
- Target Java 21 regardless of the newer JDK installed locally.

**Non-Goals:**
- Choosing the application's architecture, packages for the domain, or packaging as an executable JAR.

## Decisions

- **Generate with `mvn archetype:generate` (`org.apache.maven.archetypes:maven-archetype-quickstart`) in batch mode** rather than hand-writing files, so the structure is genuinely archetype-produced. Coordinates: `groupId=es.upm.amusementpark`, `artifactId=amusement-park`, `package=es.upm.amusementpark`, version `1.0-SNAPSHOT`. Files are generated in a temp directory and moved to the repository root (the repo root already exists and is not empty).
- **`maven.compiler.release=21`** instead of source/target, because `release` also checks the platform API against Java 21 when building with a newer JDK (local JDK is 25).
- **Keep the archetype's sample `App` and `AppTest`** — they give the build a compile and test target; later milestones replace them.

## Risks / Trade-offs

- [Archetype download requires network/Maven Central] → If generation fails offline, stop and report rather than hand-crafting a divergent structure.
- [Archetype default compiler level differs from 21] → Override properties in `pom.xml` and verify with a build.
