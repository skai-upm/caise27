# Proposal

## Why

Milestone F1-C09 closes Delivery 1: every feature (create, delete, update, persistence, file/interactive input, HELP/EXIT) must work together, unexpected situations — including input that cannot be processed — must be handled in a controlled way, and the project must build from scratch with `mvn package` into a JAR runnable outside any IDE.

## What Changes

- Guarantee that no single command can terminate the application unexpectedly: any unforeseen failure while processing a line is reported as an error and processing continues.
- Add an end-to-end integration test reproducing the source's basic execution example (§16) both interactively and from `input.txt`.
- Add an automated check of the Delivery 1 technical restrictions on the sources: no regular expressions, Java standard library only, required class separation.
- Document build and run instructions in `README.md`.
- Commit with the exact message `[F1-C09] Complete delivery 1 integration`.

## Capabilities

### New Capabilities
- `delivery-integration`: Cross-feature guarantees of a delivery — the reference execution flow, robustness against unprocessable input and the technical restrictions.

### Modified Capabilities
- `command-processing`: unexpected failures while processing a command are contained and reported.

## Impact

- `CommandProcessor` (catch-all for unexpected runtime failures), new `IntegrationTest` and `TechnicalRestrictionsTest`, `README.md`.
- No new commands or business rules.
