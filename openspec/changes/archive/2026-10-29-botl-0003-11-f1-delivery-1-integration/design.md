# Design

## Context

`mvn package` currently produces `amusement-park-1.0-SNAPSHOT.jar` without a `Main-Class` manifest entry. `CommandProcessor.execute` switches on the whole trimmed line. See proposal.md for the flagged prerequisites that remain out of scope.

## Goals / Non-Goals

**Goals:** executable, IDE-independent JAR; split-based parsing; no uncontrolled failure from any input line or file argument.

**Non-Goals:** user model, validation, USER commands and persistence (their own proposals).

## Decisions

- **Manifest via `maven-jar-plugin`** `archive.manifest.mainClass = es.upm.amusementpark.App`. No shading is needed because there are no runtime dependencies. Alternative: `maven-assembly-plugin` fat jar — unnecessary.
- **`<finalName>amusement-park</finalName>`** so the artifact is `target/amusement-park.jar`.
- **Parsing:** `String[] fields = line.trim().split(";", -1)`; keyword = `fields[0].trim()`; general commands (HELP, EXIT) require exactly one field, otherwise an error "invalid arguments" is printed. `-1` keeps trailing empty fields so `HELP;` is also rejected.
- **Robustness:** `run` wraps each `execute` call in `try/catch (RuntimeException)` printing `Error: could not process command: <line>`; this keeps one bad line from stopping the run. The handler set is a `switch` on the keyword so later milestones can add commands.
- **End-to-end check:** a JUnit test cannot run `java -jar` on the JAR during `test` (the JAR is created later in `package`), so the JAR is verified by explicit commands in the tasks; behavior through `App.main` stays covered by unit tests.

## Risks / Trade-offs

- [Changing the JAR name] → only the placeholder `nombre-aplicacion.jar` is in the source; the chosen name is documented in the spec scenarios.
- [Catching RuntimeException could hide bugs] → the error is printed with the failing line, and tests cover normal paths explicitly.
