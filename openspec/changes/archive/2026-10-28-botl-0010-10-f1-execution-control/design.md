# Design

## Context

`App.run` loops over lines until end of input, calling `CommandProcessor.process`, which returns the response text. See proposal.md.

## Goals / Non-Goals

**Goals:** HELP text in one place; EXIT stops the shared loop for both sources.
**Non-Goals:** `System.exit` inside command handling.

## Decisions

- **Exit flag on the processor**: `EXIT` sets `exitRequested` and returns `Goodbye`; `App.run` checks `processor.isExitRequested()` after each line and stops reading. Alternative: a special return value or exception — a flag keeps `process` returning plain text and is easy to test.
- **HELP text** built from a constant list of syntax lines (one per line), plus the modifiable field names.
- `main` still exits via the status returned by `execute` (0), so the process terminates even if stdin stays open.

## Risks / Trade-offs

- [HELP text must be kept in sync with new commands] → later milestones update the same constant.
