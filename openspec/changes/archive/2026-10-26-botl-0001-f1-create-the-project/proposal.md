# Proposal

## Why

The project has no application code yet. Before any domain feature can be built in the lab, we need a reproducible Java 21 / Maven baseline that compiles, runs its tests and packages into an executable JAR.

## What Changes

- Generate a Maven project at the repository root from the archetype `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping its standard layout (`src/main/java`, `src/test/java`, `pom.xml`).
- Configure the build to compile for JDK 21.
- Keep the archetype's main class as the executable entry point and its sample test as the basic testing structure.
- Configure packaging so `mvn package` produces a JAR runnable with `java -jar` (manifest declares the main class).
- No application-domain functionality is added.

## Capabilities

### New Capabilities
- `project-build`: How the application is built, tested and packaged (Maven, JDK 21, quickstart layout, executable JAR).

### Modified Capabilities

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...` at the repository root.
- Tooling: requires Maven and a JDK able to target release 21.
- No existing code is affected (repository currently contains only OpenSpec scaffolding).
