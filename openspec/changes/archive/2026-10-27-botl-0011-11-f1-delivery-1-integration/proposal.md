# Proposal

## Why

Milestone F1-C09 closes Delivery 1: all features (user model, validation, creation, persistence, deletion, update, file input, HELP/EXIT) must work together, every situation that arises during use — including input that cannot be processed — must be handled in a controlled way, and the project must build from scratch with Maven into a JAR that runs without an IDE, interactively and with an external command file.

## What Changes

- Guarantee that no unexpected runtime error while processing a command terminates the application: it is reported as an error and processing continues.
- Add end-to-end tests running the assignment's basic execution example (section 16) interactively and from a command file, including rejected operations.
- Verify the Delivery 1 technical restrictions: Java 21, standard Maven structure, `mvn package`, executable JAR, standard library only at runtime, `;`/`split` command parsing without regular expressions, separate user/main/persistence classes, encapsulated attributes, file-based persistence.
- Verify a from-scratch build of a fresh clone of the repository.
- Record the mandatory milestone commit `[F1-C09] Complete delivery 1 integration` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: adds containment of unexpected errors during command processing.
- `project-build`: adds the Delivery 1 technical restrictions (standard library only at runtime, no regular expressions/databases/frameworks/encryption) and the from-scratch build requirement.

## Impact

- `CommandProcessor` (catch-all around command execution); new `Delivery1IntegrationTest`.
- No change to command syntax or outputs.
