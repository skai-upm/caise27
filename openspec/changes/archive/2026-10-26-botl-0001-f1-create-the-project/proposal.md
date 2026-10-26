# Proposal

## Why

The amusement park application has no code yet. Before any domain feature can be built, the repository needs a buildable Java 21 Maven project generated from the standard `maven-archetype-quickstart` archetype, with an executable entry point and a working test structure, so later deliveries have a stable base to grow from.

## What Changes

- Generate a Maven project at the repository root using `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping the standard layout (`pom.xml`, `src/main/java`, `src/test/java`).
- Configure the build to compile with JDK 21.
- Provide an executable main class and configure packaging so `mvn package` produces a runnable JAR (`java -jar target/parque.jar`).
- Keep the archetype's basic unit-test structure so `mvn test` succeeds.
- No application-domain functionality is added.

## Capabilities

### New Capabilities
- `project-build`: How the application is built, tested and packaged — Maven project structure, Java 21 compilation, test execution via `mvn test`, and an executable JAR with a main class.

### Modified Capabilities
<!-- None: there are no existing specs. -->

## Impact

- New files: `pom.xml`, `src/main/java/es/upm/parque/App.java`, `src/test/java/es/upm/parque/AppTest.java`.
- Build tooling: Maven and JDK 21 become required to build the project.
- No runtime dependencies beyond the Java standard library; JUnit (test scope) from the archetype.
