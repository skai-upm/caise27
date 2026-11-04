# Spec Delta

## MODIFIED Requirements

### Requirement: Command parsing
The system SHALL interpret each command line by splitting it with `String.split` on `;`, where the first element names the operation (`USER CREATE`, `USER DELETE` or `USER UPDATE`) and the remaining elements are its parameters. Each command SHALL produce one result message beginning with `OK` on success or `ERROR` on failure. Blank lines are not commands and SHALL be skipped by the input loop without a result message.

#### Scenario: Unknown operation
- **WHEN** `USER RENAME;ana@correo.com` is processed
- **THEN** an `ERROR` message is produced, no data changes, and the application keeps accepting commands

#### Scenario: Wrong parameter count
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an `ERROR` message is produced and no user is created

#### Scenario: Empty or garbage line
- **WHEN** `hello` is processed, or an empty line is read
- **THEN** `hello` produces an `ERROR` message, the empty line produces no message, and the application does not terminate

### Requirement: Terminal interaction
In interactive mode (no launch arguments) the application SHALL read commands line by line from standard input, print each command's result message, and continue until `EXIT` is processed or the input ends.

#### Scenario: Several commands in one session
- **WHEN** a create command followed by an invalid command and a delete command are entered
- **THEN** three result lines are printed and the session ends normally at end of input
