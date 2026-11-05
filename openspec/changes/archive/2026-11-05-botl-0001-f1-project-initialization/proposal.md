# Proposal

## Why

The Amusement Park Java application has no code yet: the repository only holds a license, README and an empty OpenSpec setup. Milestone F0-C01 (Delivery 1, step 1) requires a Maven project built from the `maven-archetype-quickstart` archetype, configured for JDK 21 and compilable with Maven. Every later delivery depends on this structure, and projects with a different structure are not accepted.

## What Changes

- Generate a Maven project at the repository root using the Apache `maven-archetype-quickstart` archetype (standard `src/main/java` / `src/test/java` layout, `pom.xml`).
- Configure the build for JDK 21 (`maven.compiler.release` = 21) with UTF-8 source encoding.
- Keep the archetype's generated sample class and test so `mvn compile` / `mvn test` succeed on a clean checkout.
- Record the milestone-mandated message `[F0-C01] Initialize Maven project with JDK 21` with the commit that delivers this change.

### In scope
- Maven project structure, `pom.xml`, JDK 21 configuration, successful compilation.

### Out of scope
- Any domain behavior (users, commands, park entities, persistence), executable packaging (later milestone F1), extra plugins or dependencies not needed for compilation.

## Capabilities

### New Capabilities
- `project-initialization`: Build baseline of the application — Maven quickstart structure, JDK 21 target, and a compilable project.

### Modified Capabilities
<!-- None: no existing specs in openspec/specs/. -->

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...`.
- Build tooling: requires JDK 21+ and Maven on the developer machine.
- No existing behavior is affected (greenfield repository).
