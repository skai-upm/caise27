# Proposal

## Why

Milestone F0-C02 requires the application to be buildable and runnable outside the development environment: `mvn package` must produce a JAR that starts with `java -jar`, either interactively (commands from standard input) or processing a command file passed as an argument. Correct JAR generation and execution are mandatory for every delivery, so this must be in place before commands are added.

## What Changes

- Configure the Maven build so `mvn package` produces an executable JAR whose manifest declares the application entry point.
- Give the JAR a stable file name in `target/` so it can be started with `java -jar target/<name>.jar`.
- Implement the entry point: with no arguments it reads commands line by line from standard input until end of input; with one argument it reads commands line by line from that text file.
- Introduce a single line-processing loop shared by both modes, so later milestones only add command handling.
- Commit with the mandatory message `[F0-C02] Configure executable JAR`.

## Capabilities

### New Capabilities
- `execution-modes`: How the application is started (`java -jar`) and where it reads commands from (interactive standard input vs. a text file argument).

### Modified Capabilities
- `project-build`: `mvn package` must now produce an executable JAR with a declared entry point.

## Impact

- `pom.xml` (jar plugin manifest, final name), `src/main/java/es/upm/amusementpark/App.java`, new tests.
- Ambiguities flagged (no business rule invented):
  - The source uses the placeholder `nombre-aplicacion.jar`; this change names the JAR `amusement-park.jar` (artifactId) — the name is not otherwise specified.
  - No commands exist yet in this milestone, so lines read are accepted and produce no output; command behavior is defined by later milestones.
  - Behavior for a missing/unreadable input file is not specified; this change reports an error on standard error and exits with a non-zero status rather than silently doing nothing.
