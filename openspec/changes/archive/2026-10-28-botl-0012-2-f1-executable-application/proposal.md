# Proposal

## Why

Milestone F0-C02 requires the project to be an application that Maven packages into a JAR started with `java -jar target/<application-name>.jar`, interactively or with a command-file argument, with a correctly defined entry point. Because this batch applied the Delivery 1 integration milestone (F1-C09) first, the manifest entry point and both execution modes already exist; however the produced JAR still carries the version-dependent default name `amusement-park-1.0-SNAPSHOT.jar`, unlike the application-named form `target/nombre-aplicacion.jar` used by the source, and nothing guards the entry-point configuration against regressions.

## What Changes

- Configure the build so `mvn package` produces `target/amusement-park.jar` (application name, version independent).
- Add an automated check that the configured `Main-Class` is the application entry point and that it exposes the standard `main` method.
- Verify `java -jar target/amusement-park.jar` interactively and with `input.txt`.
- Commit with the exact message `[F0-C02] Configure executable JAR`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `application-execution`: the executable JAR requirement names the produced file `target/amusement-park.jar` and requires a defined entry point.

## Impact

- `pom.xml` (`finalName`), new test, documentation of the launch commands in the specs.
- **Flagged ambiguities / ordering conflicts:**
  - `nombre-aplicacion` in the source is a placeholder for the application name; `amusement-park` (the artifactId) is used.
  - Behaviour already delivered by F1-C09 (executable manifest, interactive and file modes) is verified, not re-implemented. Mandatory-commit order (§15) cannot match the roadmap because of the batch order; flagged previously in F1-C09.
