# Proposal

## Why

Milestone F1-C09 closes Delivery 1: all features must work together, any input that cannot be processed must be handled in a controlled manner, and the technical restrictions (Java standard library only, no regular expressions, file persistence, String/split command processing, separated classes) must hold. A review of the current code shows two gaps: the email check splits on `"\\."` (regular-expression syntax), and runtime failures such as an I/O error while saving or an unreadable `users.txt` at start-up would end the application with an uncaught exception.

## What Changes

- Any failure while processing a command (including unexpected internal errors such as a failed save) produces an `ERROR: ...` message and the application continues with the next command.
- An unreadable users file at start-up is reported with an `ERROR` message and a controlled exit (status 1) instead of a stack trace.
- Remove regular-expression usage from production code (email validation uses `indexOf`/`substring` and `split` on `@`).
- Record the Delivery 1 technical restrictions as verifiable requirements and add an end-to-end test of the assignment's basic execution example in both modes.
- Out of scope: Delivery 2 features.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `command-interface`: adds controlled handling of commands whose processing fails unexpectedly.
- `user-persistence`: adds controlled handling of an unreadable users file at start-up.
- `project-initialization`: adds the Delivery 1 technical restrictions (standard library only, no regular expressions, no databases/frameworks/encryption).

## Impact

- `CommandProcessor` (catch-all), `App` (start-up failure), `UserValidator.isValidEmail` (no regex); new end-to-end tests. No change to command syntax or file format.
