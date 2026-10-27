# Design

## Context

`App.main` already reads commands from standard input or from the file named by the first argument (change F1-C08, applied earlier in this repository). The JAR built by the quickstart pom has no `Main-Class`, so `java -jar` cannot start it.

## Goals / Non-Goals

**Goals:**
- `mvn package` → `target/amusement-park.jar` startable with `java -jar`.

**Non-Goals:**
- Shading/fat JARs: Delivery 1 forbids external libraries, so the application has no runtime dependencies to bundle.
- Changes to command processing.

## Decisions

- **maven-jar-plugin manifest** (`<archive><manifest><mainClass>es.upm.amusementpark.App</mainClass></manifest></archive>`) configured in `<build><plugins>`. Alternatives: maven-assembly/shade plugins — unnecessary without runtime dependencies.
- **JAR name (assumption, flagged):** the source uses `target/nombre-aplicacion.jar`, a placeholder for "application name". `<finalName>${project.artifactId}</finalName>` yields `target/amusement-park.jar` (no version suffix), keeping the command stable across versions.
- **Mandatory commit:** `[F0-C02] Configure executable JAR` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Evaluators expect a specific JAR name] → Name derived from artifactId; change `finalName` if a fixed name is later required.
