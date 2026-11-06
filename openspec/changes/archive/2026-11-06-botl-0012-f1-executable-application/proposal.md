# Proposal

## Why

Milestone F0-C02 requires the Maven project to produce, with `mvn package`, a JAR that runs with `java -jar` independently of the IDE and declares the application entry point. It must start in interactive mode without arguments and process a command file given as an argument. Because of the order in which milestones were processed, this behavior was already delivered by the Delivery 1 integration change and is specified in the `application-execution` capability. This change records the milestone and re-verifies it against the current code, without duplicating requirements.

## What Changes

- No new behavior and no code change. The existing requirements "Executable JAR built with Maven", "Interactive mode", "File mode" and "Supported execution forms" in `application-execution` already cover the milestone.
- Re-verify the milestone from a clean build: the JAR is produced at `target/amusement-park.jar`, its manifest names `es.upm.park.App`, and it runs outside the project directory in interactive and file mode.

### In scope
- Verification of F0-C02 against the current build.

### Out of scope
- Any change to packaging, entry point or execution modes.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- None. The change declares `skip_specs: true` because no spec-level behavior changes. The milestone's behavior is already in `openspec/specs/application-execution/spec.md`.

## Impact

- No code or spec changes. This is a verification-only record for milestone F0-C02.
