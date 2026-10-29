# Proposal

## Why

Milestone F0-C02 requires the Maven project to produce, with `mvn package`, an executable JAR that runs independently of the development environment with `java -jar`, starting in interactive mode without arguments and processing a text file given as argument, with the entry-point class correctly defined. In this batch the executable JAR was already configured by the Delivery 1 integration change (applied earlier because of the batch order), and the `project-build` spec already contains these requirements.

## What Changes

- No behavior or spec change: verify that the current build satisfies every F0-C02 requirement — `mvn package` succeeds, the manifest declares `es.upm.amusementpark.App`, and `java -jar target/amusement-park.jar` works interactively and with `input.txt`, outside any IDE.
- Record the milestone's mandatory commit message `[F0-C02] Configure executable JAR` in this change's commit.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- None. The change declares `skip_specs: true`: the requirements are already in `openspec/specs/project-build/spec.md` (Requirement: Executable JAR).

## Impact

- No source changes expected; verification only.

## Ambiguities (flagged)

- The source's JAR name `nombre-aplicacion.jar` is a placeholder; the established name is `target/amusement-park.jar`.
- The Git-history requirement expects `[F0-C02]` right after `[F0-C01]`; the batch order makes that impossible, so the message is recorded in this commit's body as for the other milestones.
