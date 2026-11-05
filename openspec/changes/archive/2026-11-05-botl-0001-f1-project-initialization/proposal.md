# Proposal

## Why

Milestone F0-C01 (Delivery 1, step 1) of the Amusement Park assignment requires a Maven project created from the `maven-archetype-quickstart` archetype, configured for JDK 21, and compilable with Maven. The repository currently contains no Java project at all, and every later delivery is rejected unless this structure exists.

## What Changes

- Add a root `pom.xml` generated from the `maven-archetype-quickstart` archetype, configured to compile for Java 21 (`maven.compiler.release` = 21).
- Add the standard Maven directory layout: `src/main/java/<package>/App.java` and `src/test/java/<package>/AppTest.java` as produced by the archetype.
- Verify the project compiles and its archetype test passes with `mvn verify`.
- Record the mandated milestone commit message `[F0-C01] Initialize Maven project with JDK 21`.

In scope: Maven project structure, JDK 21 configuration, successful build.

Out of scope: any Amusement Park domain behavior, executable command interface (F0-C02 onwards), extra dependencies, plugins or tooling not required by the archetype and JDK 21 configuration.

## Capabilities

### New Capabilities
- `project-initialization`: The project build baseline — Maven structure generated from the quickstart archetype, JDK 21 compilation target, and successful Maven compilation.

### Modified Capabilities
- None (no existing specs in `openspec/specs/`).

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Build tooling: Maven (3.9.x available) and a JDK capable of `--release 21`.
- No existing code or APIs are affected; this is the first code in the repository.
