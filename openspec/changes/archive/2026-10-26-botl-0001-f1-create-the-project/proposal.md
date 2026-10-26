# Proposal

## Why

The amusement-park application needs a buildable starting point before any domain functionality can be developed. This first lab step establishes a standard Maven project on Java 21 so that every later epic builds, tests, and packages the same way.

## What Changes

- Generate a Maven project from the `org.apache.maven.archetypes:maven-archetype-quickstart` archetype and keep its standard layout (`src/main/java`, `src/test/java`, `pom.xml`).
- Configure the build to compile for Java 21.
- Keep the archetype's main class as the application entry point and configure the build so `mvn package` produces an executable JAR that runs that main class.
- Keep the archetype's basic unit-test structure so `mvn test` compiles and runs tests successfully.
- No application-domain functionality (users, groups, tickets, attractions, etc.) is added.

## Capabilities

### New Capabilities
- `project-build`: How the application is built, tested, and packaged (Maven, Java 21, executable JAR, test structure).

### Modified Capabilities
<!-- None: there are no existing specs. -->

## Impact

- New files: `pom.xml`, the main class under `src/main/java`, and its test under `src/test/java`.
- Build tooling: Maven and a JDK able to compile for release 21.
- No runtime dependencies beyond the Java standard library; the test framework is a test-scoped dependency from the archetype.
