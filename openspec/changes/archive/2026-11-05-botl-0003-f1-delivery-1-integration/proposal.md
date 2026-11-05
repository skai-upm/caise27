# Proposal

## Why

Milestone F1-C09 (Delivery 1 integration) requires that everything developed so far works together, that the project builds from scratch with `mvn package` into a JAR that runs without an IDE both interactively and with an external command file, that unprocessable input is handled in a controlled manner, and that the Delivery 1 technical restrictions are met. Today `mvn package` produces a non-executable JAR (no entry point in the manifest), and an unexpected failure inside a command would abort the whole session.

## What Changes

- Configure the Maven build so `mvn package` produces `target/amusement-park.jar` whose manifest declares the application entry point, runnable with `java -jar target/amusement-park.jar` (interactive) and `java -jar target/amusement-park.jar input.txt` (command file).
- Make command processing resilient: a failure while executing one command is reported as an error message and processing continues with the next line (the session never ends with a stack trace).
- Add automated checks for the Delivery 1 technical restrictions that are verifiable today: Java 21, standard library only at runtime (no compile/runtime dependencies), no regular expressions and no encryption APIs in production code.
- Add an end-to-end test that runs the packaged JAR in both modes.

### In scope
- Executable packaging, controlled error handling, restriction checks, integration test of the currently implemented commands (`HELP`, `EXIT`).

### Out of scope / deferred
- `USER CREATE/DELETE/UPDATE`, user model, validation and file persistence: these are separate Delivery 1 milestones not yet present in the code base (the batch processes them later); their integration is verified when they are applied. No business rule from those milestones is invented here.
- Submission logistics (GitLab URL, uploading the JAR) and the mandated Git history: process concerns, not system behavior. The mandated message `[F1-C09] Complete delivery 1 integration` is recorded in this change's commit.

## Capabilities

### New Capabilities
- `application-runtime`: How the application is built, packaged and launched as a standalone executable JAR, the technical restrictions of the runtime, and its controlled behavior on failing input.

### Modified Capabilities
<!-- None: execution-control requirements are unchanged (unknown-command handling already exists); project-initialization unchanged. -->

## Impact

- `pom.xml` (finalName, jar manifest main class).
- `CommandProcessor` (exception containment).
- New tests: restriction checks, packaged-JAR integration test.
