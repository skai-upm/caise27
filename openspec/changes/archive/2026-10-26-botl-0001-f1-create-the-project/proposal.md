# Proposal

## Why

The amusement-park application needs a buildable starting point before any domain functionality is developed. This first lab step establishes a standard Maven project on Java 21, generated from the official quickstart archetype, so that later deliveries can build on a known, testable structure.

## What Changes

- Generate a Maven project at the repository root from `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping the archetype's standard layout (`src/main/java`, `src/test/java`, `pom.xml`).
- Configure the build to compile for Java 21.
- Provide an executable main class (the archetype's `App`) and configure the JAR manifest so `java -jar` runs it.
- Keep the archetype's basic unit-test structure so `mvn test` compiles and passes.
- No application-domain functionality is introduced.

## Capabilities

### New Capabilities
- `project-build`: How the application is built, tested and packaged (Maven, Java 21, executable JAR, test structure).

### Modified Capabilities
<!-- none -->

## Impact

- New files: `pom.xml`, `src/main/java/**/App.java`, `src/test/java/**/AppTest.java`.
- Build tooling: Maven 3.9.x with a JDK able to target release 21.
- No runtime dependencies beyond the Java standard library; JUnit is a test-scoped dependency from the archetype.
