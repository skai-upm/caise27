# Design

## Context

The code already has a single shared command loop (`CommandRunner`) used by `App` for standard input and for a file argument (see `execution-control`). What is missing for Delivery 1 integration is an executable artifact and robustness at the loop boundary. The source restricts Delivery 1 to the Java standard library with basic String operations (no regex, frameworks or external libraries).

## Goals / Non-Goals

**Goals:**
- `java -jar target/amusement-park.jar [file]` works after `mvn package`.
- One place in the loop is responsible for robustness: blank lines and failure isolation.

**Non-Goals:**
- Shading or bundling dependencies. There are no runtime dependencies to bundle.
- Implementing USER features (separate milestones).

## Decisions

- **Manifest via `maven-jar-plugin`** (`archive/manifest/mainClass = es.upm.park.App`) with `<finalName>amusement-park</finalName>`. Alternatives: the shade or assembly plugins. Rejected because there are no runtime dependencies to bundle, and the restrictions discourage extra machinery.
- **Blank-line skipping in `CommandRunner`**, not in `CommandProcessor`. The processor keeps rejecting any non-command text it receives. Deciding which lines count as input is the loop's job, and keeping it in the loop applies the rule to both sources at once.
- **Failure isolation**: `CommandRunner` catches `RuntimeException` around each `process` call and prints `ERROR: Could not process command: <line>`. Catching here, not in each command, gives one consistent behavior for future commands. `Error`s (for example OutOfMemoryError) are not caught.
- **End-to-end verification** runs the built JAR with `java -jar` on a temporary command file and on piped standard input, as a scripted check in the tasks. A Maven integration-test plugin (failsafe) is not added, which keeps the build minimal.

## Risks / Trade-offs

- [Catching RuntimeException may hide programming errors] → The message is printed and unit tests cover the expected behavior, so failures stay visible.
- [JAR name differs from the source's placeholder `nombre-aplicacion.jar`] → The source uses a placeholder name, and `amusement-park.jar` follows the artifactId.
