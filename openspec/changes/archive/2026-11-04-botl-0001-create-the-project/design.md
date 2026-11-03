# Design

## Context

The repository holds only `LICENSE`, `README.md`, `.gitignore` (already ignoring `target/`) and the OpenSpec tree. There is no existing code or spec. The build machine provides a JDK newer than 21, so the build must explicitly target release 21 rather than rely on the default JDK level.

## Goals / Non-Goals

**Goals:**
- Generate the project with `maven-archetype-quickstart` and keep its output structure.
- Pin the compiler to Java 21 and make the JAR executable.

**Non-Goals:**
- Any command protocol, domain model or persistence.
- Fat/shaded JARs, extra plugins or dependencies beyond what the archetype and executable packaging need.

## Decisions

- **Generate with the archetype, at the repository root.** Run `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart` non-interactively, then move the generated `pom.xml` and `src/` to the repository root (the repo already exists, so a nested project directory would be an unnecessary level). Alternative — hand-writing the files — rejected because the source requires the archetype.
- **Coordinates:** `groupId=com.parque`, `artifactId=parque`, `version=1.0-SNAPSHOT`, package `com.parque`. The source names none; `parque` matches the JAR name used by later deliveries' examples (`target/parque.jar`) without adding a `finalName` that this change does not need.
- **Java level:** set `maven.compiler.release=21` (preferred over `source`/`target` because it also restricts the API surface to JDK 21) and `project.build.sourceEncoding=UTF-8`.
- **Executable JAR:** configure `maven-jar-plugin` `archive/manifest/mainClass=com.parque.App`. Since there are no runtime dependencies, a plain JAR is executable; no assembly/shade plugin is needed.
- **Tests:** keep the archetype's `AppTest` and its JUnit dependency as generated (the archetype version decides JUnit 4 vs JUnit 5); only adjust if it fails to run on JDK 21.

## Risks / Trade-offs

- [Archetype download needs network access] → If generation is impossible offline, reproduce the archetype's exact output by hand and record it in tasks.
- [Archetype default plugin versions may be too old for a recent JDK] → Upgrade only the specific plugin that fails, keeping the layout unchanged.
