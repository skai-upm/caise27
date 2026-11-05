# Design

## Context

The repository has no Java code yet (see proposal.md - Why). The source assignment mandates JDK 21, Maven and the `maven-archetype-quickstart` archetype. The local build machine provides Maven 3.9 running on a newer JDK (24/25); JDK 21 itself is not installed.

## Goals / Non-Goals

**Goals:**
- Produce exactly the quickstart layout (`pom.xml`, `App`, `AppTest`) so later milestones can build on it.
- Guarantee Java 21 bytecode regardless of the newer JDK used locally.

**Non-Goals:**
- No application behavior beyond the archetype's placeholder `App`.
- No packaging/executable-jar configuration (that is F0-C02).

## Decisions

- **Generate with the archetype layout, written by hand**: reproduce the files `maven-archetype-quickstart` (1.4/1.5 style) generates rather than running `archetype:generate` interactively; the result is identical in structure and avoids network-dependent interactive generation. Alternative: running `mvn archetype:generate` non-interactively — acceptable but adds noise (extra plugin pins) without benefit.
- **`maven.compiler.release=21`** instead of `source`/`target`: `--release` also checks API usage against the Java 21 platform, so building with a newer JDK cannot accidentally use post-21 APIs.
- **Coordinates**: `groupId` `es.upm.amusementpark`, `artifactId` `amusement-park`, version `1.0-SNAPSHOT`, package `es.upm.amusementpark`. The source leaves naming to the student; these are neutral and can be kept by later milestones.
- **JUnit 5 (Jupiter)** as the test dependency, with an explicit Surefire version that supports it, matching the modern quickstart archetype.

## Risks / Trade-offs

- [JDK 21 not installed locally] → `--release 21` produces Java 21 class files (major 65) on JDK 24+; verified by inspecting the class file version.
- [Plugin versions drift] → pin compiler and surefire plugin versions in `pom.xml`.
