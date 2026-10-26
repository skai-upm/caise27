# Design

## Context

Final E3 code: layered packages, `App` (modes and wiring), `CommandProcessor` with catalog-driven help. Existing tests are mostly per component plus some `AppTest` flows. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- One readable script that tells the whole story of the park, verified automatically in every mode.

**Non-Goals:**
- New features or behavior changes.

## Decisions

- **Script as test resource** (`src/test/resources/integration/delivery3.txt`), loaded from the classpath and copied to a temp file for file mode, so it can also be run by hand with `java -jar`.
- **Assertions on stable responses**: compare the output line by line with the reviewed expected output kept next to the script (`delivery3.expected`); responses contain no salted hashes or OS-specific values, so exact comparison is stable. HELP and HELP EXTENDED are checked against the command catalog.
- **Modes**: file mode with `data` dir in a temp folder (then files checked), interactive mode with the script on stdin (same output), `--fly` (same output, no `data/`, empty `tmp/`).
- **JAR smoke check** done manually in the task (output identical to the expected file).

## Risks / Trade-offs

- [Expected file drift when messages change] → it is the point: message changes become visible in review.
