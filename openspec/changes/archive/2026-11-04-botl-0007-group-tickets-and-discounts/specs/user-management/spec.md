## MODIFIED Requirements

### Requirement: Command format and responses
The system SHALL read commands as text lines, split them on `;` into an operation followed by parameters, and answer every command with a response whose first line starts with `OK` on success or `ERROR:` on rejection. Success responses SHALL start with `OK:` unless the source of a command prescribes an exact text (`TICKET CREATE` answers `OK TICKET CREATED -> <Precio>` and `GROUPTICKET CREATE` answers `OK GROUP TICKET CREATED -> <Precio>`). Responses to `USER` commands SHALL consist of exactly one line; general commands such as `HELP` MAY add further lines.

#### Scenario: Success response
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** a single line starting with `OK:` is printed

#### Scenario: Rejection response
- **WHEN** any command is rejected
- **THEN** a single line starting with `ERROR:` explaining the reason is printed

#### Scenario: Multi-line general response
- **WHEN** `HELP` is processed
- **THEN** the first printed line starts with `OK:` and the command list follows on the next lines

#### Scenario: Prescribed success text
- **WHEN** a valid `TICKET CREATE` is processed at price 40.00
- **THEN** the single printed line is `OK TICKET CREATED -> 40.00`

#### Scenario: Prescribed group ticket text
- **WHEN** a group of 2 buys a group ticket at price 40.00
- **THEN** the single printed line is `OK GROUP TICKET CREATED -> 80.00`
