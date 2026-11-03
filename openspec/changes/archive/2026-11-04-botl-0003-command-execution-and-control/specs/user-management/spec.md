## MODIFIED Requirements

### Requirement: Command format and responses
The system SHALL read commands as text lines, split them on `;` into an operation followed by parameters, and answer every command with a response whose first line starts with `OK:` on success or `ERROR:` on rejection. Responses to `USER` commands SHALL consist of exactly that one line; general commands such as `HELP` MAY add further lines after it.

#### Scenario: Success response
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** a single line starting with `OK:` is printed

#### Scenario: Rejection response
- **WHEN** any command is rejected
- **THEN** a single line starting with `ERROR:` explaining the reason is printed

#### Scenario: Multi-line general response
- **WHEN** `HELP` is processed
- **THEN** the first printed line starts with `OK:` and the command list follows on the next lines
