# Proposal

## Why

Milestone F0-C02 (Delivery 1, step 2) requires the Amusement Park application to be built and run independently of the IDE: `mvn package` must produce a JAR that starts with `java -jar` and waits for commands on standard input. The current baseline (`project-initialization`) only compiles a placeholder `App`; the JAR has no entry point and the program does not read input.

## What Changes

- Configure Maven packaging so `mvn package` produces `target/amusement-park.jar` whose manifest declares the application entry point.
- Turn the entry point into an interactive session: on start it reads commands line by line from standard input until end of input, then terminates normally.
- In scope: executable JAR, entry-point definition, interactive start-up waiting on standard input.
- Out of scope (deferred): processing a command file given as argument (F1-C07), concrete commands such as `USER ...`, `HELP`, `EXIT` (F1-C03..F1-C08), error handling for unrecognised input (F1-C09). The project-initialization requirements are kept unchanged.

## Capabilities

### New Capabilities
- `executable-application`: How the application is packaged and launched from a terminal (executable JAR, entry point, interactive standard-input mode).

### Modified Capabilities
<!-- None: project-initialization requirements still hold unchanged. -->

## Impact

- `pom.xml`: jar plugin manifest configuration and a fixed `finalName`.
- `src/main/java/es/upm/amusementpark/App.java`: becomes the launcher of an interactive session.
- New tests for the session loop and for the packaged JAR manifest.
