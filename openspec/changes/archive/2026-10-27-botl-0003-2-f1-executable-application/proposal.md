# Proposal

## Why

Milestone F0-C02 requires the project to become an application that runs independently of the development environment: `mvn package` must produce a JAR that starts with `java -jar`, in interactive mode without arguments and processing a command file when one is given. Correct JAR generation and execution are mandatory for every delivery.

## What Changes

- Configure the Maven JAR packaging so the produced JAR declares the application entry point (`es.upm.amusementpark.App`) in its manifest.
- Give the JAR a stable file name under `target/` (`target/amusement-park.jar`).
- Verify the JAR starts in interactive mode reading standard input, and processes a command file passed as its argument (the command loop already exists from F1-C08).
- Record the mandatory milestone commit `[F0-C02] Configure executable JAR` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `project-build`: adds the requirement that `mvn package` produces an executable JAR runnable with `java -jar`, interactively and with a command file.

## Impact

- `pom.xml` (maven-jar-plugin manifest, `finalName`).
- No source code change: `App.main` already reads standard input or the file given as first argument.
