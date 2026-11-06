# Proposal

## Why

Milestone **F0-C02** (Delivery 1, step 2) requires the application to be built and run without an IDE. `mvn package` must produce a JAR that starts with `java -jar` (section 2.2). This is mandatory for every later delivery. The current baseline (`project-initialization`) only compiles. Its JAR has no entry point and cannot be launched.

## What Changes

- Configure the Maven JAR manifest with the application entry-point class, so the packaged JAR can be launched with `java -jar`.
- Give the JAR a stable file name: `target/amusement-park.jar`, the concrete name behind the assignment's `target/nombre-aplicacion.jar`.
- Add a dedicated entry-point class that starts the application.
- The milestone's mandated Git message is `[F0-C02] Configure executable JAR`.

### In scope
- Executable JAR generation via `mvn package`, the entry-point definition, and launching with `java -jar` (milestone F0-C02, section 2.2).

### Out of scope (deferred)
- Interactive command reading from standard input and processing a commands file passed as `input.txt`. Section 2.2 describes them, but they depend on the command interpreter introduced by later milestones (command sequences and execution control). This change only requires that the JAR starts. It may accept an argument, but does not interpret it yet.

## Capabilities

### New Capabilities
- `executable-application`: packaging the application as a self-launching JAR and its entry point.

### Modified Capabilities
- None. `project-initialization` requirements (structure, JDK 21, compilation) are unchanged and preserved.

## Impact

- `pom.xml`: `maven-jar-plugin` manifest configuration and `finalName`.
- New entry-point class under `src/main/java/es/upm/park/`. The archetype placeholder `App` is replaced by the real entry point.
- Build output: `target/amusement-park.jar`.
