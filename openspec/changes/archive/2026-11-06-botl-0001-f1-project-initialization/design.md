# Design

## Context

The repository is empty apart from OpenSpec scaffolding (see proposal.md - Why). The machine has Maven and a JDK newer than 21, so the 21 target must be pinned explicitly in the build rather than inferred from the installed JDK.

## Goals / Non-Goals

**Goals:**
- Use the real `maven-archetype-quickstart` output as the starting point, so the structure is the archetype's own.
- Pin the compiler to `release 21`.

**Non-Goals:**
- Choosing the application architecture, packaging an executable jar, or adding runtime dependencies.

## Decisions

- **Generate with `mvn archetype:generate`** (`org.apache.maven.archetypes:maven-archetype-quickstart`), using groupId `es.upm.park`, artifactId `amusement-park`, package `es.upm.park`. The files are generated in a temporary directory and copied to the repository root, because the repository root already exists and holds other files. Hand-writing the structure was rejected because the assignment requires the archetype.
- **`maven.compiler.release=21`** instead of `source`/`target`. `release` also restricts the JDK API to Java 21, so code built on a newer JDK cannot use post-21 APIs.
- **Keep the archetype's JUnit test.** It proves that the test phase runs and gives later milestones a test harness. If the archetype pins an old JUnit 4 version, upgrade it to JUnit Jupiter 5. This is a test-only dependency and does not change runtime behavior.

## Risks / Trade-offs

- [The installed JDK is newer than 21] → `--release 21` guarantees Java 21 bytecode and API compatibility.
- [Archetype generation needs network access to Maven Central] → If generation fails, stop and report it rather than hand-craft a fake archetype output.
