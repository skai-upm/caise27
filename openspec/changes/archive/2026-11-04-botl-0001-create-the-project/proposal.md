# Proposal

## Why

The repository currently contains only licensing, a README and the OpenSpec scaffolding — there is no buildable application. Before any amusement-park functionality can be introduced, we need a reproducible Java 21 / Maven skeleton, generated from the standard `maven-archetype-quickstart` archetype, that compiles, runs its tests and packages into an executable JAR.

## What Changes

- Add a Maven project at the repository root with the layout produced by `org.apache.maven.archetypes:maven-archetype-quickstart` (`pom.xml`, `src/main/java/<package>/App.java`, `src/test/java/<package>/AppTest.java`).
- Configure the build to compile for Java 21 (`maven.compiler.release=21`) with UTF-8 sources.
- Keep the archetype's executable entry-point class (`App` with `public static void main(String[])`).
- Keep the archetype's basic JUnit test structure so that `mvn test` compiles and runs at least one test.
- Configure `maven-jar-plugin` so `mvn package` produces a JAR whose manifest declares the entry-point class as `Main-Class`, making it runnable with `java -jar`.
- No amusement-park domain functionality (users, commands, tickets, groups, attractions, persistence) is added.

## Capabilities

### New Capabilities
- `application-bootstrap`: The buildable application skeleton — Maven/JDK 21 toolchain, Quickstart standard layout, executable entry point, executable JAR packaging and the baseline test setup.

### Modified Capabilities
- None (no existing specs).

## Impact

- **New files**: `pom.xml`, `src/main/java/**/App.java`, `src/test/java/**/AppTest.java`.
- **Build/tooling**: Maven 3.x and a JDK able to target release 21 are required to build. Test dependency: JUnit (as provided by the archetype). No runtime dependencies are added.
- **Ignored output**: `target/` is already excluded by `.gitignore`.
- **Assumptions recorded**: the archetype's generated `groupId`/`artifactId`/package are chosen by us (no names are given in the source request); the archetype version and its JUnit version are kept as generated unless they prevent building on JDK 21.
