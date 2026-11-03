# Design

## Context

The repository only contains OpenSpec scaffolding, a README and a LICENSE. See proposal.md for motivation. The machine provides Maven 3.9 and JDK 25 (no JDK 21 is installed).

## Goals / Non-Goals

**Goals:** a Quickstart-generated project at the repository root that compiles for Java 21, runs tests and packages into an executable JAR.

**Non-Goals:** command protocol, domain model, extra plugins or dependencies beyond what executable packaging needs.

## Decisions

- **Archetype generation in batch mode** with `groupId=es.upm.etsisi`, `artifactId=amusement-park`, `package=es.upm.etsisi.amusementpark`, generated in a temporary directory and moved to the repository root, so the root keeps README/LICENSE/openspec. Alternative (nested module directory) rejected: an extra directory level adds nothing.
- **Java 21 via `maven.compiler.release=21`** (replacing the archetype's default release value). `--release` makes javac on any newer JDK enforce the Java 21 language level and API, which satisfies "compiles with JDK 21" on this machine without a JDK 21 installation. Toolchains rejected: they would require a JDK 21 that is not installed.
- **Executable JAR via `maven-jar-plugin` manifest `mainClass`** = `es.upm.etsisi.amusementpark.App`. No shade/assembly plugin is needed because there are no runtime dependencies.
- Entry point: `src/main/java/es/upm/etsisi/amusementpark/App.java`; test: `src/test/java/es/upm/etsisi/amusementpark/AppTest.java` (JUnit 5 from the archetype).

## Risks / Trade-offs

- [JDK 21 not installed; build verified on JDK 25 with `--release 21`] → `--release` guarantees API/bytecode compatibility; the build will run unchanged on JDK 21.
- [Archetype version drift] → archetype version pinned in the generation command recorded in tasks.
