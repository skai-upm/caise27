# Proposal

## Why

Milestone F0-C02 (Delivery 1, step 2) requires the application to be built with `mvn package` into a JAR that starts with `java -jar`, independently of any IDE, with a correctly declared entry-point class. JAR generation and execution are mandatory for every later delivery. Today `mvn package` produces a plain library JAR with no entry point, so `java -jar` fails with "no main manifest attribute".

## What Changes

- Configure the Maven JAR packaging so the produced JAR declares the application entry-point class in its manifest.
- Give the JAR a stable name, `target/amusement-park.jar`, used in place of the source's placeholder `target/nombre-aplicacion.jar`.
- Declare `es.upm.parque.App` as the entry point.
- Verify that `mvn package` builds the JAR and `java -jar target/amusement-park.jar` starts and terminates normally.

In scope: executable JAR packaging, entry-point declaration, launching from a terminal.

Out of scope (deferred to milestone F1 "running command sequences" and later): interactive command reading from standard input, processing an `input.txt` argument, and any command semantics. Source §2.2 describes these launch modes, but this milestone only requires the JAR to be buildable and runnable.

## Capabilities

### New Capabilities
- `executable-application`: How the application is packaged and launched: building an executable JAR with `mvn package` and starting it with `java -jar`.

### Modified Capabilities
- None. `project-initialization` requirements (Maven structure, JDK 21, compilation) are preserved unchanged.

## Impact

- `pom.xml`: JAR plugin manifest configuration and `finalName`.
- `src/main/java/es/upm/parque/App.java`: remains the entry point.
- New test covering entry-point startup.
