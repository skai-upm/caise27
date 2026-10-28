# Proposal

## Why

Milestone F1-C09 closes Delivery 1: all user features must work together, any input that cannot be processed must be handled in a controlled way, the project must build from scratch with Maven and the JAR must run without an IDE both interactively and with an instructor-supplied command file. The mandatory roadmap commits must all be present in order.

## What Changes

- Guarantee that no command input, however malformed, terminates the application unexpectedly: unexpected runtime failures while processing a command are reported as `ERROR:` lines and processing continues.
- Verify the complete illustrative session of the statement (create ×2, update phone, update email, delete, HELP, EXIT) end to end in file and interactive mode, and the build from a clean checkout (`mvn clean package`).
- Document build, execution and command syntax in `README.md`; ignore the runtime data file `users.txt` in Git.
- Audit Delivery 1 technical restrictions (Java 21, Maven standard structure, standard library only, no regular expressions/databases/frameworks/encryption, separated user/main/persistence classes) and the mandatory commit roadmap.
- Commit with the mandatory message `[F1-C09] Complete delivery 1 integration`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `command-interface`: adds the guarantee that unexpected failures during a command are reported and do not terminate the application.
- `project-build`: adds the Delivery 1 technical restrictions and the mandatory commit roadmap as verifiable build/project requirements.

## Impact

- `CommandProcessor` (catch-all for unexpected runtime failures), `README.md`, `.gitignore`, integration tests.
- Ambiguity flagged: `String.split` is used for command and email parsing as the statement requires; with single-character literal separators (`;`, `@`) it is used as plain String splitting, not as a regular-expression feature. No other regex API (`matches`, `Pattern`, `replaceAll`) is used in production code.
