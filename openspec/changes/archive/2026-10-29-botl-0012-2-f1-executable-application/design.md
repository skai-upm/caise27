# Design

## Context

`pom.xml` already sets `<finalName>amusement-park</finalName>` and `maven-jar-plugin` `mainClass` `es.upm.amusementpark.App`; there are no runtime dependencies, so a plain JAR is self-contained.

## Goals / Non-Goals

**Goals:** confirm F0-C02 is met by the current build. **Non-Goals:** any new behavior.

## Decisions

- **Verification outside the IDE and outside the project directory**: run the JAR from a scratch working directory by absolute path, so the check does not depend on the project tree (also keeps runtime `data/` out of the repository).
- **No new automated test**: the JAR is produced after the `test` phase; the existing `App` tests cover both input modes, and the packaged artifact is verified by commands in the tasks.

## Risks / Trade-offs

- [No automated packaging test] → manual verification commands are recorded in tasks and repeated by later integration milestones.
