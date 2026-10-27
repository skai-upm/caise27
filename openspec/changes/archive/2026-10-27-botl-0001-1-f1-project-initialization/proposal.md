# Proposal

## Why

Milestone F0-C01 of Amusement Park Delivery 1 requires the course project to exist as a Maven project built from the `maven-archetype-quickstart` archetype, configured for JDK 21 and compilable with Maven. Every later delivery builds on this structure ("Java projects created or submitted using a different structure will not be accepted"), so it must be established first.

## What Changes

- Create the Maven project at the repository root with the standard quickstart layout (`pom.xml`, `src/main/java`, `src/test/java`).
- Configure the build to compile for Java 21 (`maven.compiler.release=21`).
- Keep the archetype's placeholder application class and its unit test so `mvn package` compiles and runs a test.
- Commit `pom.xml` and the basic structure with the exact message `[F0-C01] Initialize Maven project with JDK 21`.

No business features (users, commands, park) are introduced; they belong to later milestones.

## Capabilities

### New Capabilities
- `project-build`: The Maven build contract of the project — archetype layout, JDK 21 target and successful compilation/packaging with Maven.

### Modified Capabilities
<!-- none -->

## Impact

- New files: `pom.xml`, `src/main/java/es/upm/amusementpark/App.java`, `src/test/java/es/upm/amusementpark/AppTest.java`.
- Requires a JDK ≥ 21 and Maven on the build machine.
- Ambiguity flagged: the source does not specify `groupId`, `artifactId` or package name; `es.upm` / `amusement-park` / `es.upm.amusementpark` are chosen as neutral placeholders and are not contractual.
