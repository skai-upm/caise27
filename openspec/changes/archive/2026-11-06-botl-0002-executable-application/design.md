# Design

## Context

The `project-initialization` baseline has the archetype placeholder `es.upm.park.App` and default `maven-jar-plugin` settings, which produce no `Main-Class` manifest entry. See proposal.md for the motivation.

## Goals / Non-Goals

**Goals:** a launchable JAR with a predictable name, and a single, explicit entry point.

**Non-Goals:** command interpretation, interactive input, or reading a commands file. Later milestones add these. Third-party runtime dependencies are not needed, so no shading or fat JAR.

## Decisions

- **`maven-jar-plugin` manifest `mainClass`.** Set it in the existing `pluginManagement` version (3.4.2) under `build/plugins`. The alternatives were `maven-shade-plugin` or `maven-assembly-plugin`. Both were rejected because there are no runtime dependencies to bundle, and the plain jar plugin is the simplest standard solution.
- **`finalName` = `amusement-park`.** This drops the version suffix, so the documented command `java -jar target/amusement-park.jar` is stable.
- **Entry point `es.upm.park.Main`.** It replaces the archetype `App`, and its test moves with it. Main is a thin launcher that later milestones will extend to start the interpreter. For now it prints a startup banner, so a launch is observable.

## Risks / Trade-offs

- [Renaming the entry point breaks references to `App`] → `App` is only an archetype placeholder with no callers, and its test is migrated.
- [Before the interpreter exists, `java -jar` exits right after starting instead of waiting for input] → This is an accepted, documented deferral to the command-interpreter milestones.
