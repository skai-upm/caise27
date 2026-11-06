# Design

## Context

The repository has only OpenSpec scaffolding, a license and a README. There is no Java code yet. Section 2.1 of the assignment requires the `maven-archetype-quickstart` archetype, Maven, and JDK 21. See proposal.md for the motivation.

## Goals / Non-Goals

**Goals:**
- Provide a root-level Maven quickstart layout that compiles and tests successfully.
- Pin the compilation target to Java 21.

**Non-Goals:**
- Executable packaging (the next milestone), application code and dependencies beyond the archetype defaults.

## Decisions

- **Root-level project.** `pom.xml` sits at the repository root, so that `mvn` works from a clean checkout. The alternative was a subdirectory module, rejected because it adds a level the assignment does not ask for.
- **`maven.compiler.release=21`.** Use `release` rather than separate `source`/`target` settings. It also restricts the visible JDK API to Java 21, which is stricter when building on newer JDKs.
- **Archetype coordinates.** Use groupId `es.upm.park`, artifactId `amusement-park` and version `1.0-SNAPSHOT`. Keep the archetype's `App` class and `AppTest` (JUnit 5, as in the current archetype) as placeholders.

## Risks / Trade-offs

- [The build machine has no JDK 21, only a newer JDK] → `release 21` gives Java 21 API and bytecode compatibility. Acceptance on a real JDK 21 should still be confirmed by graders.
- [Archetype generation needs network access to Maven Central] → The archetype is cached locally. If generation fails, write the equivalent files by hand, matching the quickstart output.
