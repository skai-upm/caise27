# Proposal

## Why

Milestone F0-C01 of the Amusement Park course project (Delivery 1) requires the repository to start from a correctly structured Maven project built with JDK 21 and created with the `maven-archetype-quickstart` archetype. Every later delivery builds on this structure, and projects with a different structure are not accepted, so it has to exist before any functional work.

## What Changes

- Generate a Maven project in the repository root from the Apache `maven-archetype-quickstart` archetype (standard `src/main/java` / `src/test/java` layout, `pom.xml`).
- Configure the build to compile for Java 21 (`maven.compiler.release` = 21) with UTF-8 sources.
- Verify that `mvn package` compiles the project and runs its (archetype-generated) test successfully.
- Commit `pom.xml` and the basic project structure with the mandatory message `[F0-C01] Initialize Maven project with JDK 21`.

## Capabilities

### New Capabilities
- `project-build`: How the project is structured and built (Maven, JDK 21, quickstart layout) and the guarantee that it compiles and packages with Maven.

### Modified Capabilities
- None.

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...` in the repository root.
- Build tooling: Maven and a JDK able to target release 21.
- Git history: one milestone commit with the exact contractual message.
- Ambiguity flagged: the source text does not specify Maven coordinates (groupId/artifactId) or the base package; this change uses `es.upm` / `amusement-park` and package `es.upm.amusementpark` as an assumption, which can be revisited without changing any behavioral requirement.
