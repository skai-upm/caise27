# Design

## Context

`Main` loops over input lines, sending each to `CommandProcessor.process` and printing the response; the loop ends only at end of input.

## Goals / Non-Goals

**Goals:** HELP text; EXIT that stops the shared loop for both input sources.
**Non-Goals:** confirmation prompts, extended help (Delivery 3).

## Decisions

- **Exit flag on the processor** (`isExitRequested()`) set by EXIT; `Main`'s single reading loop checks it after each command and stops. Alternative: `System.exit` inside the processor — rejected because it skips cleanup, breaks tests and duplicates termination logic.
- **HELP text** is a constant built from one line per command; responses with several lines are returned as one string with `\n` separators (`println` adds the final newline).
- Persistence needs no flush on exit because every change is saved immediately.
