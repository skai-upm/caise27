# Proposal

## Why

Milestone F0-C02 requires the project to evolve from a basic Maven project into an application that can be built with `mvn package` and run outside the IDE with `java -jar`, with a correctly declared entry point. Correct JAR generation and execution are mandatory for every delivery.

## What Changes

- Configure the Maven JAR manifest with the application entry point so `mvn package` produces a JAR runnable with `java -jar`.
- Name the produced artifact `target/nombre-aplicacion.jar`, the exact path used by the source's run commands.
- Add the entry point class: with no arguments it starts in interactive mode reading standard input; with one argument it reads the given text file.
- Commit with the exact message `[F0-C02] Configure executable JAR`.

Command interpretation (which commands exist and what they print) is **not** part of this milestone; it arrives with the user and command-sequence milestones (F1-C01…F1-C08). This change only provides the two input channels.

## Capabilities

### New Capabilities
- `execution-modes`: How the application obtains its input when launched — interactive standard input or a text file passed as argument.

### Modified Capabilities
- `project-build`: adds the executable-JAR requirements (manifest entry point, artifact name, runnable with `java -jar`).

## Impact

- `pom.xml`: `finalName` and `maven-jar-plugin` manifest configuration.
- New `src/main/java/es/upm/amusementpark/Main.java` (entry point) replacing the archetype `App` placeholder as the launched class.
- Ambiguity flagged: `nombre-aplicacion` is a placeholder name in the source; it is kept literally because path names are treated as contractual. Behaviour for a missing/unreadable input file is unspecified; the application reports an error on standard error and exits with non-zero status rather than inventing further rules.
