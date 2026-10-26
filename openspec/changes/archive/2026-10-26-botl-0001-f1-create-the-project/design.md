# Design

## Context

The repository currently contains only OpenSpec scaffolding, a README and a LICENSE. See proposal.md for motivation. The local machine provides Maven 3.9 running on a JDK newer than 21 (no JDK 21 installation is available).

## Goals / Non-Goals

**Goals:**
- Archetype-generated layout at the repository root so later deliveries extend one project.
- Java 21 as the compilation target, enforced by the compiler plugin.
- `java -jar` works on the packaged artifact.

**Non-Goals:**
- Any domain code, extra dependencies, or plugins beyond what packaging needs.

## Decisions

- **Generate with `maven-archetype-quickstart` (version 1.5) in batch mode** with `groupId=es.upm.parque`, `artifactId=parque`, `package=es.upm.parque`, then place the generated `pom.xml` and `src/` at the repository root. Alternative: hand-writing the pom — rejected because the requirement is to use the archetype's standard structure.
- **Use `maven.compiler.release=21`** instead of `source`/`target`. `release` also restricts the API to Java 21, which is the closest guarantee of "compiles with JDK 21" while building on a newer JDK. Alternative: install JDK 21 and use toolchains — out of scope for the lab environment.
- **Executable JAR via `maven-jar-plugin` manifest `mainClass`**. No shading is needed because there are no runtime dependencies. Alternative: `maven-assembly-plugin` — unnecessary now.
- **Keep the archetype's JUnit test (`AppTest`)** as the basic test structure.

## Risks / Trade-offs

- [Building on JDK > 21 could hide JDK-21-specific issues] → `release=21` restricts the platform API and bytecode level to 21.
- [Archetype version drift changes generated files] → pin archetype version 1.5 in the generation command.
