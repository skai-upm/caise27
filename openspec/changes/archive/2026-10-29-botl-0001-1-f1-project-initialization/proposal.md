# Proposal

## Why

Milestone F0-C01 of Amusement Park Delivery 1 requires the course project to start from a correctly structured Maven project, created with the apache-maven-quickstart archetype, configured for JDK 21 and compilable with Maven. Every later delivery depends on this structure ("Java projects created or submitted using a different structure will not be accepted in any subsequent course delivery"), and the repository currently contains no Java project at all.

## What Changes

- Generate a Maven project at the repository root using the `maven-archetype-quickstart` archetype (standard `src/main/java` / `src/test/java` layout, `pom.xml`).
- Configure the build to compile for JDK 21 (`maven.compiler.release` = 21).
- Ensure `mvn package` (compile + test) succeeds with the generated skeleton and its sample test.
- Commit the `pom.xml` and basic project structure. The milestone's mandatory commit message `[F0-C01] Initialize Maven project with JDK 21` is recorded in the commit for this change (see design for how it coexists with the batch commit format).

## Capabilities

### New Capabilities
- `project-build`: The Maven project structure, target JDK and build guarantees the application must satisfy (compilable and packageable with Maven).

### Modified Capabilities
- None.

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...` at the repository root.
- Build tooling: Maven 3.x, JDK 21 language level. `target/` is already ignored by `.gitignore`.
- No runtime behavior yet; later milestones (executable JAR, domain, commands) build on this project.
