# Design

## Context

Current code: `App` (stdin or file argument) + `CommandProcessor` with `HELP`/`EXIT` (see `openspec/specs/execution-control`). USER milestones are not implemented yet because of batch ordering. See proposal.md - Why.

## Goals / Non-Goals

**Goals:** executable JAR via the standard `maven-jar-plugin` manifest; robustness of the processing loop; automated restriction checks.

**Non-Goals:** shading/fat JAR (no runtime dependencies exist), USER features.

## Decisions

- **`maven-jar-plugin` `archive/manifest/mainClass`** = `es.upm.amusementpark.App`, `finalName` = `amusement-park`. Alternatives: shade/assembly plugins — unnecessary without runtime dependencies and against "standard library only" spirit.
- **Exception containment in `CommandProcessor.execute`**: catch `RuntimeException` from a handler, print `ERROR: Could not process command: <line>` and continue. Containment at one point keeps all future commands covered.
- **Restriction checks as unit tests** scanning `src/main/java` text and `pom.xml` (plain String operations, no regex). Cheap guard against regressions in later milestones.
- **Packaged-JAR integration test** via `maven-failsafe`? Rejected to keep the build simple: the JAR is exercised by a manual/scripted verification task after `mvn package`, while unit tests cover the same `App.run` path.

## Risks / Trade-offs

- [Source scan is textual] → may flag a comment mentioning a forbidden API; acceptable, messages explain which file.
