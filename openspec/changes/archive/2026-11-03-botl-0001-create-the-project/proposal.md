# Proposal

## Why

The amusement-park application has no code yet. A reproducible Java 21 Maven skeleton, generated from the standard Quickstart archetype and with working tests and executable packaging, is needed before any domain functionality is introduced in later changes.

## What Changes

- Generate the project with `org.apache.maven.archetypes:maven-archetype-quickstart` at the repository root (Maven, Java 21).
- Keep the standard Quickstart layout (`pom.xml`, `src/main/java`, `src/test/java`), its entry-point class and its basic JUnit test.
- Configure the build to produce an executable JAR whose manifest names the entry-point class.
- Verify `mvn test` and `mvn package` succeed and the packaged JAR starts with `java -jar`.
- No amusement-park business functionality is added.

## Capabilities

### New Capabilities
- `application-bootstrap`: Maven/Java 21 build toolchain, standard Quickstart project structure, executable entry point and packaging, and the basic test infrastructure.

### Modified Capabilities
_None._

## Impact

- New files: `pom.xml`, `src/main/java/.../App.java`, `src/test/java/.../AppTest.java`.
- `.gitignore` already excludes Maven `target/`.
- No external runtime dependencies; JUnit (test scope) from the archetype.
