# Proposal

## Why

Milestone F0-C01 of Amusement Park Delivery 1 requires the project to start as a Maven project created from the Apache Maven quickstart archetype, configured for JDK 21 and compilable with Maven. Every later milestone builds on this structure, and projects with a different structure are not accepted in any later delivery, so it must exist first.

## What Changes

- Create a Maven project at the repository root generated from the `maven-archetype-quickstart` archetype (standard `src/main/java` / `src/test/java` layout, `pom.xml`).
- Configure the build to compile for JDK 21 (`maven.compiler.release` = 21).
- Ensure `mvn package` compiles the project and runs the archetype's unit test successfully.
- Record the mandatory milestone commit `[F0-C01] Initialize Maven project with JDK 21` once implementation is verified.

## Capabilities

### New Capabilities
- `project-build`: How the application is structured and built with Maven (archetype layout, JDK level, build success).

### Modified Capabilities
<!-- None: no existing specs in the project. -->

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Build tooling: Maven 3 and a JDK ≥ 21 are required to build.
- No application behavior yet; later milestones (F0-C02 onward) extend this project.
