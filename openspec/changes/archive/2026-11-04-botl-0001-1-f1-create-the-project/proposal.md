# Proposal

## Why

The repository contains no application yet. Before any amusement-park functionality can be built, the project needs a reproducible Java 21 / Maven skeleton, generated from the standard Quickstart archetype, with a working test setup and an executable JAR, so every later change starts from a known-good build.

## What Changes

- Generate the project from `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping its standard layout (`pom.xml`, `src/main/java`, `src/test/java`).
- Configure the build to compile for Java 21.
- Keep an executable entry-point (main) class and the generated basic unit-test structure.
- Configure packaging so `mvn package` produces a JAR whose manifest declares the main class, runnable with `java -jar`.
- Confirm `mvn test` succeeds.
- No amusement-park domain functionality is added.

Note: the source proposal suggests the change ID `bootstrap-java21-maven-project`; this batch uses the BOTL change ID `botl-0001-1-f1-create-the-project` for traceability. Scope is unchanged.

## Capabilities

### New Capabilities
- `application-bootstrap`: Build toolchain (Maven, Java 21), standard project layout, executable entry point, executable JAR packaging, and test infrastructure.

### Modified Capabilities
- None.

## Impact

- New files: `pom.xml`, `src/main/java/**/App.java`, `src/test/java/**/AppTest.java`.
- Build dependencies: JUnit (test scope, from the archetype); Maven JAR plugin configuration for the manifest main class.
- `target/` is already gitignored.
