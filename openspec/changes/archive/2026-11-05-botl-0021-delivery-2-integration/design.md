# Design

## Context

Commands are registered in `CommandProcessor` with usage strings; HELP is generated from registration order.

## Goals / Non-Goals

**Goals:** match the assignment's HELP list; prove the integrated behavior. **Non-Goals:** semantic changes.

## Decisions

- **Re-register commands in the assignment's order with its exact usage strings** (e.g. `<FirstName>`, `ATTRACTION STATUS;<attractionIdentifier>`); field names for USER UPDATE remain as a note after the syntax.
- **Update earlier tests** that asserted the Delivery 1 placeholder spelling (`<firstName>`), since the MODIFIED requirement supersedes it.
- **Compliance test extended** with serialization tokens and reflection checks on `User`/`Group` fields.
- **End-to-end test** runs `App.run` on a temp working directory with `--fly file` then `file`, compares outputs and checks that `tmp/` is empty.

## Risks / Trade-offs

- None.
