# Proposal

## Why

Milestone F0-C01 (Delivery 1, step 1) requires the Amusement Park application to start from a correctly structured Maven project targeting JDK 21, created from the `maven-archetype-quickstart` archetype. The repository currently holds only OpenSpec scaffolding, a README and a LICENSE, so no buildable Java project exists yet; every later milestone depends on it.

## What Changes

- Add a Maven `pom.xml` at the repository root, following the `maven-archetype-quickstart` layout, configured to compile for Java 21.
- Add the standard Maven source layout: `src/main/java/<package>/App.java` and `src/test/java/<package>/AppTest.java`.
- Configure a JUnit test dependency so `mvn test` executes the generated test.
- Out of scope: any domain model, commands, CLI behavior, persistence or other later milestones (F0-C02 onwards); any optional plugins not needed for compilation.

## Capabilities

### New Capabilities
- `project-initialization`: The build baseline of the application — Maven project structure, Java 21 target and successful compilation.

### Modified Capabilities
<!-- None: openspec/specs/ contains no capabilities yet. -->

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...`.
- Build tooling: requires Maven and a JDK able to compile with `--release 21` (JDK 21 or newer).
- No existing code or specs are affected (greenfield repository).
