# Proposal

## Why

Milestone F0-C01 of Amusement Park Delivery 1 requires the course project to exist as a Maven project created from the `maven-archetype-quickstart` archetype, configured for JDK 21, compilable with Maven, and committed with the mandatory message `[F0-C01] Initialize Maven project with JDK 21`. Every later milestone builds on this structure, and projects with a different structure are not accepted in any later delivery (source §2.1).

## What Changes

- Add a `pom.xml` produced from the Apache Maven quickstart archetype layout, targeting Java release 21.
- Add the standard quickstart source tree: `src/main/java/<package>/App.java` and `src/test/java/<package>/AppTest.java`.
- Ensure `mvn compile`, `mvn test` and `mvn package` succeed.
- Create the mandatory Git commit `[F0-C01] Initialize Maven project with JDK 21` containing the pom.xml and the basic structure.

## Capabilities

### New Capabilities
- `project-build`: The project's build contract: Maven quickstart structure, JDK 21 target and successful compilation/packaging.

### Modified Capabilities
<!-- none -->

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...`.
- Build tooling: Maven 3.9 with a JDK that supports `--release 21`.
- No runtime behaviour beyond the archetype's placeholder application.
- Ambiguity flagged: the source does not fix groupId/artifactId/package; the archetype's convention is used with a project-specific value (see design.md).
