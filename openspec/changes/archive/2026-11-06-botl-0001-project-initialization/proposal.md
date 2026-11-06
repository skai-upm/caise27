# Proposal

## Why

The Amusement Park application needs a valid starting point (milestone **F0-C01**, Delivery 1 step 1). Section 2.1 of the assignment requires that the project is a Maven project created from the `maven-archetype-quickstart` archetype and configured for JDK 21. Submissions with a different structure are rejected in every later delivery, so this baseline has to be correct before any feature work starts.

## What Changes

- Add a Maven project at the repository root, generated from `maven-archetype-quickstart`, with the standard `src/main/java` and `src/test/java` layout.
- Configure `pom.xml` so that sources compile for Java release 21.
- Keep the archetype's placeholder entry point and its test so that `mvn compile` and `mvn test` both run successfully.
- The milestone's mandated Git message is `[F0-C01] Initialize Maven project with JDK 21`.

### In scope
- The Maven structure, `pom.xml`, the JDK 21 configuration and a successful build (section 2.1, milestone F0-C01).

### Out of scope
- Any application behavior: commands, the user domain, persistence and executable packaging. These belong to later milestones (F1-C02 and later).

## Capabilities

### New Capabilities
- `project-initialization`: the build baseline of the application. It covers the Maven project structure, the JDK 21 target and successful compilation with Maven.

### Modified Capabilities
- None. There are no existing specs in `openspec/specs/`.

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Tooling: Maven 3.9+ and a JDK of version 21 or later. The compiler `release` is pinned to 21.
- No existing code is affected. The repository contains no Java sources yet.

### Unresolved questions
- The proposal refers to a bundled `SOURCE_deliverable_1.md`, but that file was not provided. The verbatim excerpts in the request are treated as authoritative.
- The assignment does not prescribe a Maven `groupId` or `artifactId`. This change assumes `es.upm.park` and `amusement-park`.
