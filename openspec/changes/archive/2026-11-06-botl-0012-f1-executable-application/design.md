# Design

## Context

`pom.xml` already sets `finalName` to `amusement-park` and the `maven-jar-plugin` manifest `mainClass` to `es.upm.park.App`, and `App` handles the interactive, file and `--fly` forms (see `application-execution`).

## Goals / Non-Goals

**Goals:** confirm, with evidence from a clean build, that the F0-C02 conditions hold.
**Non-Goals:** changing the packaging approach.

## Decisions

- **Verification only, `skip_specs: true`**: adding requirements would duplicate existing ones, which the request forbids. The requirements are already canonical.
- **Verify outside the repository**: run the JAR from a scratch directory to show it does not depend on the IDE, the project classpath or the working directory.

## Risks / Trade-offs

- [None of the code changes, so the milestone commit contains only the planning record] → This is intended and explained in the proposal.
