# Design

## Context

`pom.xml` already configures `maven-jar-plugin` with `mainClass` `es.upm.etsisi.amusementpark.App`; `App.main` handles no-argument (interactive), `<file>`, `--fly` and `--fly <file>`. The JAR file name is the Maven default `artifactId-version.jar`.

## Goals / Non-Goals

**Goals:** stable JAR name, automated entry-point check.

**Non-Goals:** changing argument handling or adding plugins/dependencies.

## Decisions

- **`<build><finalName>amusement-park</finalName>`**: version-independent name matching the source's `target/<application-name>.jar`. Alternative: keep default — rejected, the documented launch command would change with every version.
- **`EntryPointTest`**: reads `pom.xml` from the project base directory (Maven runs tests there), extracts the `<mainClass>` value with `indexOf`/`substring` (no regex), loads it with `Class.forName`, and asserts a `public static void main(String[])` method via reflection. This guards the configuration during `mvn package` without needing the JAR to exist yet.
