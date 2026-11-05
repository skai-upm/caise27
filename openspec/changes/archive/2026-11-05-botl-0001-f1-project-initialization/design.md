# Design

## Context

The repository holds only OpenSpec scaffolding, a README and a LICENSE; there is no Java code. See proposal.md for motivation. The local machine provides Maven 3.9.11 and JDK 25 (no JDK 21 installation is present).

## Goals / Non-Goals

**Goals:**
- Generate the project with `maven-archetype-quickstart` so the structure is exactly what the assignment accepts.
- Pin compilation to Java 21 independent of the JDK running Maven.

**Non-Goals:**
- Any application behaviour, packaging as an executable JAR, or extra plugins/dependencies (later milestones).

## Decisions

- **Generate in place at the repository root** (not in a subdirectory) so `pom.xml` sits at the root and later milestones build with a plain `mvn` command. Alternative: nested module directory — rejected as it complicates every later build command.
- **Coordinates:** `groupId` `es.upm.parque`, `artifactId` `amusement-park`, package `es.upm.parque`. The source does not prescribe coordinates; these are neutral and can be changed without affecting behaviour.
- **`maven.compiler.release=21`** instead of `source`/`target` 21: `release` also checks the API surface against Java 21, so building with a newer JDK (25 locally) still guarantees JDK 21 compatibility. Alternative: requiring a JDK 21 install — not available here; `release` gives equivalent guarantees.
- Keep the archetype's JUnit test so `mvn verify` exercises the test phase from the start.

## Risks / Trade-offs

- [Building with JDK 25 rather than 21] → `--release 21` enforces the Java 21 API and bytecode level (class major 65); verified by inspecting compiled classes.
- [Archetype version differences generate different JUnit versions] → Accept the archetype default; it is still the archetype structure.
