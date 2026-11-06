# Proposal

## Why

The Amusement Park application has no code yet: the repository contains only OpenSpec scaffolding, a README and a LICENSE. Milestone F0-C01 (Delivery 1, step 1) requires a Maven project, created from the Maven quickstart archetype and configured for JDK 21, that compiles with Maven. Every later delivery depends on this baseline, and projects with a different structure are not accepted.

## What Changes

- Add a standard Maven project (`pom.xml`, `src/main/java`, `src/test/java`) generated from the `maven-archetype-quickstart` archetype at the repository root.
- Configure the build to compile for Java release 21.
- Keep the archetype's placeholder application class and its test so that the project compiles and the test phase runs.
- Record the mandated milestone commit message `[F0-C01] Initialize Maven project with JDK 21` with the commit that delivers this change.

### In scope
- Maven structure, `pom.xml`, JDK 21 configuration, a successful `mvn compile` / `mvn test`.

### Out of scope
- Any domain behavior (users, commands, park, tickets, persistence), an executable command-line entry point (F0-C02), and any library beyond what the archetype and its test require.

## Capabilities

### New Capabilities
- `project-initialization`: the build baseline of the application, covering the Maven project layout, Java 21 compilation target and the ability to build from a clean checkout.

### Modified Capabilities
- None.

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Build tool: Maven, using the JDK installed on the machine with `--release 21`.
- No existing code or spec is affected. `target/` is already ignored by `.gitignore`.
